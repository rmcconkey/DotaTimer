package com.example.rmcconkey.dotatimer;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
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

    private TextView mainClockHours;
    private TextView mainClockMinutes;
    private TextView mainClockSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainClockHours = (TextView)findViewById(R.id.mainClockHours);
        mainClockMinutes = (TextView)findViewById(R.id.mainClockMinutes);
        mainClockSeconds = (TextView)findViewById(R.id.mainClockSeconds);

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
        final TextView tv;
        String title;
        final NumberPicker np1;
        final NumberPicker np2;


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
                tv = mainClockHours;
                np1.setVisibility(View.INVISIBLE);
                title = "Set hour value:";
                break;
            case R.id.mainClockMinutes :
                tv = mainClockMinutes;
                Toast.makeText(this, "mainClockMinutes", Toast.LENGTH_LONG).show();
                title = "Set minutes value:";
                break;
            case R.id.mainClockSeconds :
                tv = mainClockSeconds;
                Toast.makeText(this, "mainClockSeconds", Toast.LENGTH_LONG).show();
                title = "Set seconds value:";
                break;
            default :
                Toast.makeText(this, "Error: view.getId() not found", Toast.LENGTH_LONG).show();
                title = "default title";
                tv = null;
        }

        d.setTitle(title);

        Button setButton = (Button) d.findViewById(R.id.dialogButtonSet);
        Button cancelButton = (Button) d.findViewById(R.id.dialogButtonCancel);

        final Context context = this;

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = String.valueOf(np1.getValue()) + String.valueOf(np2.getValue());
                tv.setText(String.valueOf(value));
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
}
