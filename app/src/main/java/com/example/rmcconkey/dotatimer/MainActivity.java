package com.example.rmcconkey.dotatimer;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends Activity implements NumberPicker.OnValueChangeListener
{

    private final String TAG = this.getClass().getSimpleName();

    private enum TimeValueType { HOURS, MINUTES, SECONDS};
    private enum AlertType {NEUTRAL_CAMP, RUNE, AEGIS_RECLAIM};

    private TextView mainClockHours;
    private TextView mainClockMinutes;
    private TextView mainClockSeconds;

    private TextView roshanCountdown;
    private TextView aegisCountdown;

    private TextView neutralCampAlertTimeDisplay;
    private TextView runeAlertTimeDisplay;
    private TextView aegisAlertTimeDisplay;

    private Button syncButton;

    private int hours;
    private int minutes;
    private int seconds;

    private int neutralAlertTime;
    private int runeAlertTime;
    private int roshanAlertTime;
    private int aegisAlertTime;

    private boolean neutralAlertEnabled = true;
    private boolean runeAlertEnabled = true;
    private boolean roshanAlertEnabled = true;
    private boolean aegisAlertEnabled = true;

    private Switch neutralCampSwitch;
    private Switch runeSwitch;
    private Switch roshanSwitch;
    private Switch aegisSwitch;

    SharedPreferences prefs;
    private static final String PREFERENCES = "dota timer";

    private static final String NEUTRAL_SWITCH = "neutral switch";
    private static final String RUNE_SWITCH = "rune switch";
    private static final String ROSHAN_SWITCH = "roshan switch";
    private static final String AEGIS_SWITCH = "aegis switch";
    private static final String NEUTRAL_ALERT_TIME = "neutral alert time";
    private static final String RUNE_ALERT_TIME = "rune alert time";
    private static final String ROSHAN_ALERT_TIME = "roshan alert time";
    private static final String AEGIS_ALERT_TIME = "aegis alert time";

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            tick();
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainClockHours = (TextView)findViewById(R.id.mainClockHours);
        mainClockMinutes = (TextView)findViewById(R.id.mainClockMinutes);
        mainClockSeconds = (TextView)findViewById(R.id.mainClockSeconds);

        roshanCountdown = (TextView)findViewById(R.id.roshan_countdown);
        aegisCountdown = (TextView)findViewById(R.id.aegis_countdown);

        neutralCampAlertTimeDisplay = (TextView)findViewById(R.id.neutral_camp_alert_time_display);
        runeAlertTimeDisplay = (TextView)findViewById(R.id.rune_alert_time_display);
        aegisAlertTimeDisplay = (TextView)findViewById(R.id.aegis_reclaim_alert_time_display);

        neutralCampSwitch = (Switch)findViewById(R.id.neutralCampSwitch);
        neutralCampSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                neutralAlertEnabled = isChecked;
            }
        });
        runeSwitch = (Switch)findViewById(R.id.runeSwitch);
        runeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                runeAlertEnabled = isChecked;
            }
        });
        roshanSwitch = (Switch)findViewById(R.id.roshanSwitch);
        roshanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                roshanAlertEnabled = isChecked;
            }
        });
        aegisSwitch = (Switch)findViewById(R.id.aegisSwitch);
        aegisSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aegisAlertEnabled = isChecked;
            }
        });

        syncButton = (Button)findViewById(R.id.syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals("Stop")) {
                    timerHandler.removeCallbacks(timerRunnable);
                    b.setText("Sync");
                } else {
                    timerHandler.postDelayed(timerRunnable, 0);
                    b.setText("Stop");
                }
            }
        });

        prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

    }

    @Override
    public void onResume() {
        super.onResume();

        loadSettings();
        updateAlertTimers();
    }

    private void loadSettings() {
        neutralAlertTime = prefs.getInt(NEUTRAL_ALERT_TIME, 0);
        runeAlertTime = prefs.getInt(RUNE_ALERT_TIME, 0);
        roshanAlertTime = prefs.getInt(ROSHAN_ALERT_TIME, 0);
        aegisAlertTime = prefs.getInt(AEGIS_ALERT_TIME, 0);

        neutralCampSwitch.setChecked(prefs.getBoolean(NEUTRAL_SWITCH, false));
        runeSwitch.setChecked(prefs.getBoolean(RUNE_SWITCH, false));
        roshanSwitch.setChecked(prefs.getBoolean(ROSHAN_SWITCH, false));
        aegisSwitch.setChecked(prefs.getBoolean(AEGIS_SWITCH, false));
    }

    private void updateAlertTimers() {
        if (neutralAlertTime<10) {
            neutralCampAlertTimeDisplay.setText(":0" + String.valueOf(neutralAlertTime));
        } else {
            neutralCampAlertTimeDisplay.setText(":" + String.valueOf(neutralAlertTime));
        }

        if (runeAlertTime<10) {
            runeAlertTimeDisplay.setText("1:0" + String.valueOf(runeAlertTime));
        } else {
            runeAlertTimeDisplay.setText("1:" + String.valueOf(runeAlertTime));
        }

        if (aegisAlertTime<10) {
            aegisAlertTimeDisplay.setText(":0" + String.valueOf(aegisAlertTime));
        } else {
            aegisAlertTimeDisplay.setText(":" + String.valueOf(aegisAlertTime));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        syncButton.setText("Sync");

        saveSettings();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(NEUTRAL_ALERT_TIME, neutralAlertTime);
        editor.putInt(RUNE_ALERT_TIME, runeAlertTime);
        editor.putInt(ROSHAN_ALERT_TIME, roshanAlertTime);
        editor.putInt(AEGIS_ALERT_TIME, aegisAlertTime);

        editor.putBoolean(NEUTRAL_SWITCH, neutralCampSwitch.isChecked());
        editor.putBoolean(RUNE_SWITCH, runeSwitch.isChecked());
        editor.putBoolean(ROSHAN_SWITCH, roshanSwitch.isChecked());
        editor.putBoolean(AEGIS_SWITCH, aegisSwitch.isChecked());

        editor.apply();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSyncTime(View view) {

        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.dialog);

        String title;
        final NumberPicker np1;
        final NumberPicker np2;
        final TimeValueType timeValueType;

        np1 = (NumberPicker) d.findViewById(R.id.numberPicker1);
        if (np1 != null) {
            np1.setMaxValue(5);
            np1.setMinValue(0);
            np1.setWrapSelectorWheel(true);
            np1.setOnValueChangedListener(this);
        } else {
            Log.e(TAG, "Error: np1 is null");
        }

        np2 = (NumberPicker) d.findViewById(R.id.numberPicker2);
        if (np2 != null) {
            np2.setMaxValue(9);
            np2.setMinValue(0);
            np2.setWrapSelectorWheel(true);
            np2.setOnValueChangedListener(this);
        } else {
            Log.e(TAG, "Error: np2 is null");
        }

        switch (view.getId()) {
            case R.id.mainClockHours :
                np1.setVisibility(View.INVISIBLE);
                title = "Set hour value:";
                timeValueType = TimeValueType.HOURS;
                break;
            case R.id.mainClockMinutes :
                title = "Set minutes value:";
                timeValueType = TimeValueType.MINUTES;
                break;
            case R.id.mainClockSeconds :
                title = "Set seconds value:";
                timeValueType = TimeValueType.SECONDS;
                break;
            default :
                Toast.makeText(this, "Error: view.getId() not found", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: view.getId() not found");
                title = "default title";
                timeValueType = null;
        }

        d.setTitle(title);

        Button setButton = (Button) d.findViewById(R.id.dialogButtonSet);
        Button cancelButton = (Button) d.findViewById(R.id.dialogButtonCancel);

        final Context context = this;
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeValueType == TimeValueType.HOURS) {
                    hours = np2.getValue();
                } else if (timeValueType == TimeValueType.MINUTES) {
                    minutes = np1.getValue()*10 + np2.getValue();
                } else if (timeValueType == TimeValueType.SECONDS) {
                    seconds = np1.getValue()*10 + np2.getValue();
                } else {
                    Toast.makeText(context, "Error: timeValueType not set", Toast.LENGTH_LONG).show();
                }
                updateMainClock();
                d.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        d.show();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //Toast.makeText(this, "newVal = " + newVal, Toast.LENGTH_LONG).show();
    }

    private void updateMainClock() {
        mainClockHours.setText(String.valueOf(hours));
        if (minutes<10) {
            mainClockMinutes.setText("0" + String.valueOf(minutes));
        } else {
            mainClockMinutes.setText(String.valueOf(minutes));
        }
        if (seconds<10) {
            mainClockSeconds.setText("0" + String.valueOf(seconds));
        } else {
            mainClockSeconds.setText(String.valueOf(seconds));
        }
    }

    public void resetMainClock(View view) {
        hours = 0;
        minutes = 0;
        seconds = 0;
        updateMainClock();
    }

    private void tick() {

        seconds++;
        if (seconds>59) {
            seconds = 0;
            minutes++;
        }
        if (minutes>59) {
            minutes = 0;
            hours++;
        }
        if (hours>9) {
            hours = 0;
        }
        updateMainClock();

        checkTimers();
    }

    private void checkTimers() {
        checkNeutralCampTimer();
        checkRuneTimer();
    }

    private void checkNeutralCampTimer() {
        if (seconds == neutralAlertTime && neutralAlertEnabled) {
            Toast.makeText(this, "Neutral Camp Alert!", Toast.LENGTH_LONG).show();
        }
    }

    private void checkRuneTimer() {
        if (minutes%2==1 && seconds==runeAlertTime && runeAlertEnabled) {
            Toast.makeText(this, "Rune Alert!", Toast.LENGTH_LONG).show();
        }
    }

    public void setAlertTime(View view) {
        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.dialog);

        String title = "Set alert value:";
        final NumberPicker np1;
        final NumberPicker np2;
        final AlertType alertType;

        np1 = (NumberPicker) d.findViewById(R.id.numberPicker1);
        if (np1 != null) {
            np1.setMaxValue(5);
            np1.setMinValue(0);
            np1.setWrapSelectorWheel(true);
            np1.setOnValueChangedListener(this);
        } else {
            Log.e(TAG, "Error: np1 is null");
        }

        np2 = (NumberPicker) d.findViewById(R.id.numberPicker2);
        if (np2 != null) {
            np2.setMaxValue(9);
            np2.setMinValue(0);
            np2.setWrapSelectorWheel(true);
            np2.setOnValueChangedListener(this);
        } else {
            Log.e(TAG, "Error: np2 is null");
        }

        switch (view.getId()) {
            case R.id.neutral_camp_alert_time_display :
                alertType = AlertType.NEUTRAL_CAMP;
                break;
            case R.id.rune_alert_time_display :
                alertType = AlertType.RUNE;
                break;
            case R.id.aegis_reclaim_alert_time_display :
                alertType = AlertType.AEGIS_RECLAIM;
                break;
            default :
                Toast.makeText(this, "Error: view.getId() not found", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: view.getId() not found");
                title = "default title";
                alertType = null;
        }

        d.setTitle(title);

        Button setButton = (Button) d.findViewById(R.id.dialogButtonSet);
        Button cancelButton = (Button) d.findViewById(R.id.dialogButtonCancel);

        final Context context = this;
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertType == AlertType.NEUTRAL_CAMP) {
                    neutralAlertTime = np1.getValue()*10 + np2.getValue();
                } else if (alertType == AlertType.RUNE) {
                    runeAlertTime = np1.getValue()*10 + np2.getValue();
                } else if (alertType == AlertType.AEGIS_RECLAIM.AEGIS_RECLAIM) {
                    aegisAlertTime = np1.getValue()*10 + np2.getValue();
                } else {
                    Toast.makeText(context, "Error: timeValueType not set", Toast.LENGTH_LONG).show();
                }
                updateAlertTimers();
                d.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        d.show();

    }
}
