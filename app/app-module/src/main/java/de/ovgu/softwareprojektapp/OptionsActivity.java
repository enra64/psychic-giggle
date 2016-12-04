package de.ovgu.softwareprojektapp;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.control.commands.ChangeSensorSensitivity;

public class OptionsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    static final String
            EXTRA_SERVER_ADDRESS = "Address",
            EXTRA_SERVER_PORT_COMMAND = "CommandPort";


    LinearLayout mSensorOptions;
    Button mBackBtn;
    int mNumberOfSensors;
    ArrayList<SeekBar> mSeekBars = new ArrayList<SeekBar>();
    CommandConnection mComCon;

    public static final String PREFS_NAME = "SensitivitySettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        mSensorOptions = (LinearLayout) findViewById(R.id.SensorOptions);
        mBackBtn = (Button) findViewById(R.id.backFromOptionsBtn);

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Bundle givenExtras = getIntent().getExtras();



        try {//TODO:Exception wörk wörk
            mComCon = new CommandConnection(null); //new CommandConnection from Server address and command port
            mComCon.setRemote(InetAddress.getByName(givenExtras.getString(EXTRA_SERVER_ADDRESS)),givenExtras.getInt(EXTRA_SERVER_PORT_COMMAND));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mNumberOfSensors =  SensorType.values().length;

        createSensorOptions();

        //get recently set sharedpreference values of sensorsensitivities
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        for(SeekBar s : mSeekBars){
            s.setProgress(settings.getInt(s.getTag().toString(), 50));
        }


    }
    /*
    * create Options dynamically based on the number of sensortypes
    * */
    public void createSensorOptions(){
        for (int i = 0; i < mNumberOfSensors; i++){

            TextView text = new TextView(OptionsActivity.this);
            text.setText(SensorType.values()[i].toString());
            text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            SeekBar seek = new SeekBar(OptionsActivity.this);
            seek.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            seek.setTag(SensorType.values()[i]);
            seek.setOnSeekBarChangeListener(this);

            mSeekBars.add(seek);




            mSensorOptions.addView(text);
            mSensorOptions.addView(seek);


        }
    }
    /*
    set sharedpreferences when activity is exited
     */
    protected void onPause(){
        super.onPause();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        for(SeekBar s : mSeekBars){
            editor.putInt(s.getTag().toString(), s.getProgress());
        }
        editor.commit();

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    //send new sensitivity as command
    public void onStopTrackingTouch(SeekBar seekBar) {
            new SendCommand().execute(new ChangeSensorSensitivity((SensorType)seekBar.getTag() ,seekBar.getProgress()));
    }

    /**TODO: wörk wörk
     * This class is a wrapper for {@link CommandConnection#sendCommand(AbstractCommand) sending commands}
     * to avoid dealing with network on the ui thread. Use as follows:
     * <br><br>
     * {@code new SendCommand().execute(new WhatEverCommand()); }
     * <br><br>
     * where WhatEverCommand is a subclass of {@link AbstractCommand}
     */
    private class SendCommand extends AsyncTask<AbstractCommand, Void, Void> {
        @Override
        protected Void doInBackground(AbstractCommand... commands) {
            try {
                mComCon.sendCommand(commands[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
    }
}
