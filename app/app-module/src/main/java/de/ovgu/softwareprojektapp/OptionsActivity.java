package de.ovgu.softwareprojektapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import de.ovgu.softwareprojekt.SensorType;

public class OptionsActivity extends AppCompatActivity {

    LinearLayout mSensorOptions;
    Button mBackBtn;
    int mNumberOfSensors;
    ArrayList<SeekBar> mSeekBars = new ArrayList<SeekBar>();

    public static final String PREFS_NAME = "SensitivitySettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        mSensorOptions = (LinearLayout) findViewById(R.id.SensorOptions);
        mBackBtn = (Button) findViewById(R.id.backFromOptionsBtn);

        mNumberOfSensors =  SensorType.values().length;

        createSensorOptions();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        for(SeekBar s : mSeekBars){
            s.setProgress(settings.getInt(s.getTag().toString(), 50));
        }


    }
    public void createSensorOptions(){
        for (int i = 0; i < mNumberOfSensors; i++){

            TextView text = new TextView(OptionsActivity.this);
            text.setText(SensorType.values()[i].toString());
            text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            SeekBar seek = new SeekBar(OptionsActivity.this);
            seek.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            seek.setTag(SensorType.values()[i]);

            mSeekBars.add(seek);




            mSensorOptions.addView(text);
            mSensorOptions.addView(seek);


        }
    }
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        for(SeekBar s : mSeekBars){
            editor.putInt(s.getTag().toString(), s.getProgress());
        }
        editor.commit();

    }
}
