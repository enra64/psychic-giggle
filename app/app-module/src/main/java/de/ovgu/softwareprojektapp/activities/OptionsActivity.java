package de.ovgu.softwareprojektapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.commands.ChangeSensorSensitivity;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojektapp.R;
import de.ovgu.softwareprojektapp.UiUtil;
import de.ovgu.softwareprojektapp.networking.AsyncSendCommand;

/**
 * This activity is for setting options. You may give {@link #EXTRA_SERVER_ADDRESS} and
 * {@link #EXTRA_SERVER_PORT_COMMAND}, and changes in sensor sensitivity will immediately sent to
 * that server.
 */
@SuppressLint("CommitPrefEdits")
public class OptionsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    /**
     * Intent extra keys
     */
    static final String
            EXTRA_SERVER_ADDRESS = "Address",
            EXTRA_SERVER_PORT_COMMAND = "CommandPort",
            EXTRA_ACTIVE_SENSORS = "ActiveSensor",
            EXTRA_SENSOR_DESCRIPTIONS = "SensorDescription";

    /**
     * the linear layout containing all options
     */
    private LinearLayout mSensorOptions;

    /**
     * number of sensors available
     */
    private int mNumberOfSensors;

    /**
     * a list of seekbars, each tagged with their respective SensorType value
     */
    private ArrayList<SeekBar> mSeekBars = new ArrayList<>();

    /**
     * the text views used for displaying the sensor name
     */
    private ArrayList<TextView> mTextViews = new ArrayList<>();

    /**
     * The command connection used to communicate with the server
     */
    private CommandConnection mComCon;

    /**
     * Name of the shared preferences file we use to store the settings
     */
    public static final String
            SENSITIVITY_PREFS_NAME = "SensitivitySettings",
            DISCOVERY_PREFS_NAME = "discovery_config";

    public static final String
            DISCOVERY_PREFS_PORT = "port",
            DISCOVERY_PREFS_DEVICE_NAME = "deviceName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_options);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mNumberOfSensors = SensorType.values().length;
        mSensorOptions = (LinearLayout) findViewById(R.id.SensorOptions);

        // back button listener
        findViewById(R.id.backFromOptionsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // only allows closing the activity if the port is valid
                if(isDiscoveryPortValid())
                    finish();
            }
        });

        // try to connect to server
        initialiseCommandConnection();

        // create the SeekBars and headers for the sensor settings
        createSensorOptions();
    }

    /**
     * Initialise the command connection, or go into local only mode (eg don't send to server)
     */
    private void initialiseCommandConnection(){
        Bundle givenExtras = getIntent().getExtras();
        try {
            // new CommandConnection from Server address and command port
            mComCon = new CommandConnection(null, new ExceptionListener() {
                @Override
                public void onException(Object origin, Exception exception, String info) {
                    Log.w("OriginActivity", info );
                    exception.printStackTrace();
                }
            });

            // jump to catch block if no extras were given
            if(givenExtras == null)
                throw new IOException();

            // configure command connection with the given extras
            mComCon.setRemote(InetAddress.getByName(givenExtras.getString(EXTRA_SERVER_ADDRESS)), givenExtras.getInt(EXTRA_SERVER_PORT_COMMAND));
        }
        // this exception is thrown when something with the remote connection went bad
        // because we do not care, we simply go into local only mode
        catch (IOException e) {
            // notify user of local-ness
            UiUtil.showToast(this, getString(R.string.changes_only_stored_locally));

            // ensure that connection is dead
            mComCon.close();
            mComCon = null;
        }
    }


    /**
     * Create options dynamically based on the number of sensortypes
     */
    private void createSensorOptions() {
        //array of active sensors
        boolean[] sensors = getIntent().getBooleanArrayExtra(EXTRA_ACTIVE_SENSORS);
        //map of descriptions for each active sensor
        HashMap<SensorType, String> descriptionsHash = (HashMap<SensorType, String>) getIntent().getSerializableExtra(EXTRA_SENSOR_DESCRIPTIONS);


        for (int i = 0; i < mNumberOfSensors; i++) {
            //only create seekbar and text if the server needs these sensors or
            //when there is no server connection
            if(sensors == null || sensors[i]){
                // Create a header for the seekbar
                TextView text = new TextView(OptionsActivity.this);
                text.setText(SensorType.values()[i].toString());
                text.setTextSize(18f);
                text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                // create description for sensors
                TextView description = new TextView(OptionsActivity.this);
                description.setText(descriptionsHash.get(SensorType.values()[i]));
                description.setTextSize(14f);
                description.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));


                // create the seekbar
                SeekBar seek = new SeekBar(OptionsActivity.this);
                seek.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                seek.setTag(SensorType.values()[i]);
                seek.setOnSeekBarChangeListener(this);

                mSeekBars.add(seek);
                mTextViews.add(text);
                mTextViews.add(description);

                mSensorOptions.addView(text);
                mSensorOptions.addView(description);
                mSensorOptions.addView(seek);
            }
        }
    }

    /**
     * Parse the discovery port from the edittext for the discovery port
     *
     * @return true if the port is valid
     */
    private boolean isDiscoveryPortValid() {
        EditText portEditText = (EditText) findViewById(R.id.port_edit_text);
        String portFieldContent = portEditText.getText().toString();

        try {
            // try and convert the field content to a number
            int port = Integer.valueOf(portFieldContent);

            // avoid root-only ports
            if (port < 1024)
                throw new InvalidParameterException(getString(R.string.ports_below_1024_denied));

            // avoid non-existent ports
            if (port > 65535)
                throw new InvalidParameterException(getString(R.string.ports_above_65535_denied));

            // remove error if we set one, since we have successfully survived the trials
            portEditText.setError(null);
        } catch (NumberFormatException e) {
            // display an error about invalid numbers
            portEditText.setError(getString(R.string.non_number_denied));
            return false;
        } catch (InvalidParameterException e) {
            // ports below 1024 are root ports...
            portEditText.setError(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Apply the most recently saved seekbar positions to the seekbar
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadSensorConfig();
        loadDiscoveryConfig();
    }

    /**
     * set sharedpreferences when activity is exited
     */
    @Override
    protected void onPause() {
        super.onPause();
        storeSensorConfig();
        storeDiscoveryConfig();
    }

    /**
     * Overridden to deny closing the activity if the port is not valid
     */
    @Override
    public void onBackPressed() {
        if(isDiscoveryPortValid())
            super.onBackPressed();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /**
     * send new sensitivity as command
     *
     * @param seekBar the seekbar that was changed
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mComCon != null)
            new AsyncSendCommand(mComCon).execute(new ChangeSensorSensitivity((SensorType) seekBar.getTag(), seekBar.getProgress()));
    }

    /**
     * Store the current discovery configuration persistently
     */
    private void storeDiscoveryConfig() {
        SharedPreferences sharedPref = getSharedPreferences(DISCOVERY_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(DISCOVERY_PREFS_PORT, Integer.valueOf(((EditText) findViewById(R.id.port_edit_text)).getText().toString()));
        editor.putString(DISCOVERY_PREFS_DEVICE_NAME, ((EditText) findViewById(R.id.device_name_edit_text)).getText().toString());
        editor.commit();
    }

    /**
     * Load the old discovery configuration, and apply to the edittexts
     */
    private void loadDiscoveryConfig() {
        SharedPreferences sharedPref = getSharedPreferences(DISCOVERY_PREFS_NAME, Context.MODE_PRIVATE);
        int port = sharedPref.getInt(DISCOVERY_PREFS_PORT, 8888);
        String deviceName = sharedPref.getString(DISCOVERY_PREFS_DEVICE_NAME, Build.MODEL);

        // find the EditTexts for port and device name and set their text
        ((EditText) findViewById(R.id.port_edit_text)).setText(String.valueOf(port));
        ((EditText) findViewById(R.id.device_name_edit_text)).setText(deviceName);

        ((EditText) findViewById(R.id.port_edit_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // set back button to work only when the port is valid
                findViewById(R.id.backFromOptionsBtn).setEnabled(isDiscoveryPortValid());
            }
        });
    }

    /**
     * Store the current sensor configuration persistently
     */
    private void storeSensorConfig() {
        SharedPreferences settings = getSharedPreferences(SENSITIVITY_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        for (SeekBar s : mSeekBars) {
            editor.putInt(s.getTag().toString(), s.getProgress());
        }
        editor.commit();
    }

    /**
     * Load the last sensor sensitivity values, and apply them to their respective SeekBar
     */
    private void loadSensorConfig() {
        //get recently set SharedPreference values of sensor sensitivities
        SharedPreferences settings = getSharedPreferences(SENSITIVITY_PREFS_NAME, 0);
        for (SeekBar s : mSeekBars)
            s.setProgress(settings.getInt(s.getTag().toString(), 50));
    }
}
