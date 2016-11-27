package de.ovgu.softwareprojektapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import de.ovgu.softwareprojekt.SensorType;

public class OptionsActivity extends AppCompatActivity {

    LinearLayout mSensorOptions;
    Button mBackBtn;
    int mNumberOfSensors;

    //TextView[] mSensorNames = new TextView[5];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        mSensorOptions = (LinearLayout) findViewById(R.id.SensorOptions);
        mBackBtn = (Button) findViewById(R.id.backFromOptionsBtn);

        mNumberOfSensors =  SensorType.values().length-1;

        createSensorOptions();
    }
    public void createSensorOptions(){
        for (int i = 0; i < mNumberOfSensors; i++){
           // mSensorNames[i] = new TextView(OptionsActivity.this);
            //mSensorNames[i].setText(SensorType.values()[i].toString());

            TextView sensorName1 = new TextView(OptionsActivity.this);
            sensorName1.setText(SensorType.values()[i].toString());
            sensorName1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, mSensorOptions.getHeight() / (2*mNumberOfSensors)));

            SeekBar seek1 = new SeekBar(OptionsActivity.this);
            seek1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mSensorOptions.getHeight() / (2*mNumberOfSensors)));



        }
    }
}
