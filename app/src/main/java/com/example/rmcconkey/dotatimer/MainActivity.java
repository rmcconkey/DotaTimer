package com.example.rmcconkey.dotatimer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
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

import java.util.Date;

public class MainActivity extends Activity implements NumberPicker.OnValueChangeListener
{

    private final String TAG = this.getClass().getSimpleName();


    private Handler timerHandler;
    private Runnable timerRunnable;
    private int handlerDelay;

    private enum TimeValueType { HOURS, MINUTES, SECONDS}
    private enum AlertType {NEUTRAL_CAMP, RUNE, AEGIS_RECLAIM}

    private TextView mainClockHours;
    private TextView mainClockMinutes;
    private TextView mainClockSeconds;

    private TextView roshanCountdownDisplay;
    private TextView aegisCountdownDisplay;

    private TextView neutralCampAlertTimeDisplay;
    private TextView runeAlertTimeDisplay;
    private TextView aegisAlertTimeDisplay;
    private TextView dialogHelpMessage;

    private Button syncButton;

    private int hours;
    private int minutes;
    private int seconds;
    private boolean firstTick = true;

    private int neutralAlertTime;
    private int runeAlertTime;
    private int roshanAlertTime;
    private int aegisAlertTime;

    private int roshanCountdownMinutes = 8;
    private int roshanCountdownSeconds = 0;
    private int aegisCountdownMinutes = 6;
    private int aegisCountdownSeconds = 0;

    private boolean isMainClockEnabled = false;
    private boolean isRoshanCountingdown = false;
    private boolean isAegisCountingdown = false;
    private boolean isRoshanTimerReversed = false;

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
    private static final String ADJUST_BY_TEN = "adjust by ten";
    private static final String HANDLER_DELAY = "handler delay";

    private AudioManager audioManager;
    private float actVolume, maxVolume, volume;
    private int counter;
    private SoundPool soundPool;
    private int soundID;
    private boolean plays = false, loaded = false;

    private Date syncTimeStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        mainClockHours = (TextView)findViewById(R.id.mainClockHours);
        mainClockMinutes = (TextView)findViewById(R.id.mainClockMinutes);
        mainClockSeconds = (TextView)findViewById(R.id.mainClockSeconds);

        roshanCountdownDisplay = (TextView)findViewById(R.id.roshan_countdown_display);
        aegisCountdownDisplay = (TextView)findViewById(R.id.aegis_countdown_display);

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

        final Context context = this;
        syncButton = (Button)findViewById(R.id.syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals("Stop")) {
                    timerHandler.removeCallbacks(timerRunnable);
                    isMainClockEnabled = false;
                    syncTimeStart = null;
                    b.setText("Sync");
                } else {
                    timerHandler= new Handler();
                    timerRunnable = new Runnable() {

                        @Override
                        public void run() {
                            tick();
                            timerHandler.postDelayed(this, handlerDelay);
                        }
                    };
                    timerHandler.postDelayed(timerRunnable, 0);
                    Toast.makeText(context, "Handler started with delay " + handlerDelay, Toast.LENGTH_LONG).show();
                    isMainClockEnabled = true;
                    firstTick = true;
                    syncTimeStart = new Date(System.currentTimeMillis());
                    b.setText("Stop");
                }
            }
        });

        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;

        //Hardware buttons setting to adjust the media sound
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // the counter will help us recognize the stream id of the sound played
        counter = 0;

        // Load the sounds
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        soundID = soundPool.load(this, R.raw.beep_07, 1);

    }

    @Override
    public void onResume() {
        super.onResume();

        // check if this is first time app is run
        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("adjust by ten", true);
            prefs.edit().putBoolean("firstrun", false);
            prefs.edit().apply();
        }

        loadSettings();
        updateAlertTimers();
    }

    private void loadSettings() {
        neutralAlertTime = prefs.getInt(NEUTRAL_ALERT_TIME, 0);
        runeAlertTime = prefs.getInt(RUNE_ALERT_TIME, 0);
        roshanAlertTime = prefs.getInt(ROSHAN_ALERT_TIME, 0);
        aegisAlertTime = prefs.getInt(AEGIS_ALERT_TIME, 0);

        neutralCampSwitch.setChecked(prefs.getBoolean(NEUTRAL_SWITCH, false));
        neutralAlertEnabled = prefs.getBoolean(NEUTRAL_SWITCH, false);
        runeSwitch.setChecked(prefs.getBoolean(RUNE_SWITCH, false));
        runeAlertEnabled = prefs.getBoolean(RUNE_SWITCH, false);
        roshanSwitch.setChecked(prefs.getBoolean(ROSHAN_SWITCH, false));
        roshanAlertEnabled = prefs.getBoolean(ROSHAN_SWITCH, false);
        aegisSwitch.setChecked(prefs.getBoolean(AEGIS_SWITCH, false));
        aegisAlertEnabled = prefs.getBoolean(AEGIS_SWITCH, false);

        handlerDelay = prefs.getInt(HANDLER_DELAY, 1000);
    }

    private void updateAlertTimers() {
        if (neutralAlertTime<10) {
            neutralCampAlertTimeDisplay.setText(":0" + String.valueOf(neutralAlertTime));
        } else {
            neutralCampAlertTimeDisplay.setText(":" + String.valueOf(neutralAlertTime));
        }

        if (runeAlertTime<10) {
            runeAlertTimeDisplay.setText(":0" + String.valueOf(runeAlertTime));
        } else {
            runeAlertTimeDisplay.setText(":" + String.valueOf(runeAlertTime));
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
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
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

        editor.putInt(HANDLER_DELAY, handlerDelay);

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
        dialogHelpMessage = (TextView)d.findViewById(R.id.dialog_help_message);
        dialogHelpMessage.setText("");

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

        if (!firstTick) {
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
        } else {
            firstTick = false;
        }

        checkMainClockAccuracy();

        updateMainClock();

        updateCountdownTimers();

        checkTimers();
    }

    private void checkMainClockAccuracy() {
        long now = System.currentTimeMillis();
        long gameTimeLength = (now - syncTimeStart.getTime())/1000;
        long mainClockTime = hours*60*60 + minutes*60 + seconds;
        if (mainClockTime%5 == 0) {
            Toast.makeText(this, "Difference: " + Long.toString(mainClockTime - gameTimeLength), Toast.LENGTH_SHORT).show();
            if (mainClockTime-gameTimeLength > 0 && seconds != 0) {
                handlerDelay++;
                seconds--;
                playBeepSound();
                Toast.makeText(this, "handlerDelay changed to " + handlerDelay, Toast.LENGTH_LONG).show();
            } else if (mainClockTime-gameTimeLength < 0 && seconds != 0) {
                handlerDelay--;
                seconds++;
                playBeepSound();
                Toast.makeText(this, "handlerDelay changed to " + handlerDelay, Toast.LENGTH_LONG).show();
            }
        }
    }



    private void checkTimers() {
        checkNeutralCampTimer();
        checkRuneTimer();
    }

    private void checkNeutralCampTimer() {
        if (seconds == neutralAlertTime && neutralAlertEnabled) {
            Toast.makeText(this, "Neutral Camp Alert!", Toast.LENGTH_LONG).show();
            playNeutralCampWarningSound();
        }
    }

    private void checkRuneTimer() {
        if (minutes%2==1 && seconds==runeAlertTime && runeAlertEnabled) {
            Toast.makeText(this, "Rune Alert!", Toast.LENGTH_LONG).show();
            playRuneSpawnWarningSound();
        }
    }

    private void updateCountdownTimers() {
        String text = null;
        if (roshanSwitch.isChecked() && isRoshanCountingdown) {

            if (!isRoshanTimerReversed) {
                if (roshanCountdownSeconds == 0 && roshanCountdownMinutes != 0) {
                    roshanCountdownMinutes--;
                    roshanCountdownSeconds = 59;
                } else if (roshanCountdownSeconds == 0 && roshanCountdownMinutes == 0) {
                    isRoshanTimerReversed = true;
                    roshanCountdownSeconds = 1;
                } else {
                    roshanCountdownSeconds--;
                }
            } else {
                if (roshanCountdownSeconds == 59 && roshanCountdownMinutes != 2) {
                    roshanCountdownMinutes++;
                    roshanCountdownSeconds = 0;
                } else if (roshanCountdownSeconds == 59 && roshanCountdownMinutes == 2) {
                    roshanCountdownSeconds = 0;
                    roshanCountdownMinutes = 3;
                    isRoshanCountingdown = false;
                } else {
                    roshanCountdownSeconds++;
                }
            }

            if (isRoshanTimerReversed) {
                text = "-";
            } else {
                text = "";
            }
            if (roshanCountdownSeconds<10) {
                text += String.valueOf(roshanCountdownMinutes) + ":0" + String.valueOf(roshanCountdownSeconds);
            } else {
                text += String.valueOf(roshanCountdownMinutes) + ":" + String.valueOf(roshanCountdownSeconds);
            }
            roshanCountdownDisplay.setText(text);
        }

        if (aegisSwitch.isChecked() && isAegisCountingdown) {
            if (aegisCountdownSeconds == 0 && aegisCountdownMinutes != 0) {
                aegisCountdownMinutes--;
                aegisCountdownSeconds = 59;
            } else if (aegisCountdownSeconds == 0 && aegisCountdownMinutes == 0) {
                isAegisCountingdown = false;
            } else {
                aegisCountdownSeconds--;
            }
            if (aegisCountdownSeconds<10) {
                text = String.valueOf(aegisCountdownMinutes) + ":0" + String.valueOf(aegisCountdownSeconds);
            } else {
                text = String.valueOf(aegisCountdownMinutes) + ":" + String.valueOf(aegisCountdownSeconds);
            }
            aegisCountdownDisplay.setText(text);
        }
    }

    public void startRoshanCountdown(View view){
        if (isMainClockEnabled) {
            isRoshanCountingdown = true;
            isAegisCountingdown = true;
        } else {
            isRoshanCountingdown = false;
            isAegisCountingdown = false;
        }
        roshanCountdownMinutes = 8;
        roshanCountdownSeconds = 0;
        aegisCountdownMinutes = 6;
        aegisCountdownSeconds = 0;
        roshanCountdownDisplay.setText("8:00");
        aegisCountdownDisplay.setText("6:00");
        isRoshanTimerReversed = false;
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

        dialogHelpMessage = (TextView)d.findViewById(R.id.dialog_help_message);
        switch (view.getId()) {
            case R.id.neutral_camp_alert_time_display :
                alertType = AlertType.NEUTRAL_CAMP;
                dialogHelpMessage.setText(getString(R.string.neutral_camp_help_message));
                break;
            case R.id.rune_alert_time_display :
                alertType = AlertType.RUNE;
                dialogHelpMessage.setText(getString(R.string.rune_help_message));
                break;
            case R.id.aegis_reclaim_alert_time_display :
                alertType = AlertType.AEGIS_RECLAIM;
                dialogHelpMessage.setText(getString(R.string.aegis_reclaim_help_message));
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
                } else if (alertType == AlertType.AEGIS_RECLAIM) {
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

    private void playBeepSound() {
        if (loaded && !plays) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f);
            counter = counter++;
            //plays = true;
        }
    }

    private void playNeutralCampWarningSound() {
        playBeepSound();
    }

    private void playRuneSpawnWarningSound() {
        playBeepSound();
    }

    private void playRoshanRespawnSound() {
        playBeepSound();
    }

    private void playAegisReclaimSound() {
        playBeepSound();
    }
}
