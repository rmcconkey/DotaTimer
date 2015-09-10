package com.example.rmcconkey.dotatimer;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements NumberPicker.OnValueChangeListener
{

    private final String TAG = this.getClass().getSimpleName();

    private enum TimeValueType { HOURS, MINUTES, SECONDS};

    private TextView mainClockHours;
    private TextView mainClockMinutes;
    private TextView mainClockSeconds;

    private Button syncButton;

    private int hours;
    private int minutes;
    private int seconds;

    private boolean neutralAlertEnabled = true;
    private boolean runeAlertEnabled = true;
    private boolean roshanAlertEnabled = true;
    private boolean aegisAlertEnabled = true;

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

    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        syncButton.setText("Sync");
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

        checkRuneTimer();
    }

    private void checkRuneTimer() {
        if (minutes%2==1 && runeAlertEnabled) {

        }
    }
}
