package de.ovgu.softwareprojektapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.net.ConnectException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.control.commands.ConnectionAliveCheck;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequestResponse;
import de.ovgu.softwareprojekt.control.commands.ChangeSensorSensitivity;
import de.ovgu.softwareprojekt.control.commands.SetSensorCommand;
import de.ovgu.softwareprojekt.control.commands.UpdateButtons;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojektapp.networking.NetworkClient;
import de.ovgu.softwareprojektapp.sensors.SensorHandler;

public class SendActivity extends AppCompatActivity implements OnCommandListener, ExceptionListener {
    /**
     * The intent extras (the data given to us by the {@link DiscoveryActivity})
     */
    static final String
            EXTRA_SERVER_NAME = "Name",
            EXTRA_SERVER_ADDRESS = "Address",
            EXTRA_SERVER_PORT_DISCOVERY = "DiscoveryPort",
            EXTRA_SERVER_PORT_COMMAND = "CommandPort",
            EXTRA_SERVER_PORT_DATA = "DataPort",
            EXTRA_SELF_NAME = "SelfName";

    /**
     * result codes for the activity
     */
    static final int
            RESULT_SERVER_REFUSED = -1,
            RESULT_USER_STOPPED = 0,
            RESULT_SERVER_NOT_LISTENING_ON_COMMAND_PORT = -2,
            RESULT_SERVER_CONNECTION_TIMED_OUT = -3;

    /**
     * The network client organises all our communication with the server
     */
    NetworkClient mNetworkClient;

    /**
     * linear layout as a space to add buttons
     */
    private LinearLayout mRuntimeButtonLayout;

    /**
     * The SensorHandler used to handle our sensor needs
     */
    SensorHandler mSensorHandler;

    /**
     * Dialog for showing connection progress
     */
    ProgressDialog mConnectionProgressDialog;

    /**
     * Save a timestamp of when we received the last command from the server
     */
    private long mLastCommandTimestamp;

    /**
     * This timer is used to frequently check whether the connection is still alive
     */
    private Timer mConnectionAgeTimer = new Timer();

    /**
     * This constant defines the threshold after which a connectino is deemed dead, and the
     * SendActivity is stopped
     */
    private static final long MAXIMUM_CONNECTION_AGE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        // find the layout we use to display buttons requested by the server
        mRuntimeButtonLayout = (LinearLayout) findViewById(R.id.runtime_button_parent);

        // we create a NetworkDevice from our extras to have all the data we need in a neat package
        parseIncomingNetworkDevice();

        // create a new sensor handler to help with the sensors
        mSensorHandler = new SensorHandler(this, mNetworkClient);

        // disable the sensors while this button is held down
        Button disableSensorsButton = (Button) findViewById(R.id.disableSensorsButton);
        disableSensorsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // we only handle button press and release
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    mSensorHandler.temporarilyDisableSensors();
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    mSensorHandler.enableTemporarilyDisabledSensors();
                else
                    return false;

                return true;
            }
        });

        // default to successful execution
        setResult(RESULT_USER_STOPPED);

        // immediately try to connect to the server
        initiateConnection();

        // default activity result is closing by user
        setResult(RESULT_USER_STOPPED);
    }

    /**
     * Start a timer that periodically checks whether we have recently communicated with the server.
     * If the last connection is too old, it gives alarm, as the connection has probably timed out
     */
    private void startConnectionCheckTimer() {
        // set the last connection to NOW (+ execution delay of timer) to avoid immediate disconnect
        mLastCommandTimestamp = System.currentTimeMillis() + 1000;

        mConnectionAgeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if ((System.currentTimeMillis() - mLastCommandTimestamp) > MAXIMUM_CONNECTION_AGE)
                    onConnectionTimeout();
            }
        }, 1000, 500);
    }

    private void onConnectionTimeout(){
        // if the server _is_ still alive, send a last message :(
        mNetworkClient.signalConnectionEnd();

        // close the activity with an appropriate result code
        closeActivity(RESULT_SERVER_CONNECTION_TIMED_OUT);
    }

    /**
     * Initialise {@link #mNetworkClient} from incoming extras
     */
    private void parseIncomingNetworkDevice() {
        // get the extras
        Bundle in = getIntent().getExtras();

        // parse a server from incoming extras
        NetworkDevice server = new NetworkDevice(
                in.getString(EXTRA_SERVER_NAME),
                in.getInt(EXTRA_SERVER_PORT_DISCOVERY),
                in.getInt(EXTRA_SERVER_PORT_COMMAND),
                in.getInt(EXTRA_SERVER_PORT_DATA),
                in.getString(EXTRA_SERVER_ADDRESS));

        // initiate the network client with the necessary parameters
        mNetworkClient = new NetworkClient(
                server,
                getIntent().getExtras().getString(EXTRA_SELF_NAME),
                this,
                this
        );
    }

    /**
     * This function is called by a button. Currently, it starts listening to server commands and
     * requests a connection with the server given as an intent extra
     */
    public void initiateConnection() {
        // display indeterminate, not cancelable wait period to user
        mConnectionProgressDialog = ProgressDialog.show(this, "Connecting", "Waiting for server response", true, false);

        // begin listening for commands
        mNetworkClient.requestConnection();
    }

    /**
     * This function is called by a button in the action bar. It sends a command to the server which
     * then closes the connection. It also stops the activity.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_disconnect) {
            // end connections
            mNetworkClient.signalConnectionEnd();

            // close activity
            closeActivity(RESULT_USER_STOPPED);

            // handled click -> return true
            return true;
        } else if (item.getItemId() == R.id.menu_settings) {
            goToOptions();
        }
        // invoke the superclass if we didn't want to handle the click
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add all requested buttons to the action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_send, menu);
        return true;
    }

    /**
     * Sets activity result, closes connections and closes the activity
     *
     * @param result one of {@link #RESULT_USER_STOPPED} or {@link #RESULT_SERVER_REFUSED}
     */
    private void closeActivity(int result) {
        // set result of activity
        setResult(result);

        // stop generating sensor data
        mSensorHandler.closeAll();

        // close all network resources used
        mNetworkClient.close();

        // close activity
        finish();
    }

    /**
     * Enable only the required sensors
     *
     * @param requiredSensors list of required sensors
     */
    private void setSensor(final List<SensorType> requiredSensors) {
        // configure the sensor run state
        if (!mSensorHandler.setRunning(requiredSensors))
            UiUtil.showAlert(this, "Sensor activation error", "This device does not contain a required sensor");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorHandler.temporarilyDisableSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorHandler.enableTemporarilyDisabledSensors();
        SharedPreferences sensitivitySettings = getSharedPreferences(OptionsActivity.PREFS_NAME, 0);
        for (SensorType s : SensorType.values()) {
            mNetworkClient.sendCommand(new ChangeSensorSensitivity(s, sensitivitySettings.getInt(s.toString(), 50)));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNetworkClient.signalConnectionEnd();
        mNetworkClient.close();
    }

    /**
     * Handle connection request responses
     *
     * @param granted true if the request was granted
     */
    private void handleConnectionResponse(boolean granted) {
        // the progress dialog can be dismissed in any case
        mConnectionProgressDialog.dismiss();

        if (granted){
            UiUtil.showToast(this, getString(R.string.send_activity_connection_success));


            startConnectionCheckTimer();
        }
        else
            closeActivity(RESULT_SERVER_REFUSED);
    }

    /**
     * Called whenever a new command comes in
     *
     * @param origin  where the command comes from
     * @param command the command itself
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCommand(InetAddress origin, AbstractCommand command) {
        // decide what to do with the command
        switch (command.getCommandType()) {
            case ConnectionRequestResponse:
                ConnectionRequestResponse res = (ConnectionRequestResponse) command;
                handleConnectionResponse(res.grant);
                break;
            case SetSensor:
                // enable or disable a sensor
                SetSensorCommand com = (SetSensorCommand) command;
                setSensor(com.requiredSensors);
                break;
            //adds a button to the activity with id and name
            case AddButton:
                //create button with name and id
                UpdateButtons addCom = (UpdateButtons) command;
                createButtons(addCom);
                break;
            case ConnectionAliveCheck:
                mLastCommandTimestamp = System.currentTimeMillis();
                ConnectionAliveCheck response = (ConnectionAliveCheck) command;
                response.answerer = mNetworkClient.getSelf();
                mNetworkClient.sendCommand(response);
                break;
            // ignore unhandled commands
            default:
                break;
        }
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        exception.printStackTrace();

        // this exception is thrown when we send commands, but the server has closed its command port
        if (exception instanceof ConnectException && exception.getMessage().contains("ECONNREFUSED")) {
            // close the non-user-closeable connecting... dialog
            if (mConnectionProgressDialog != null)
                mConnectionProgressDialog.dismiss();

            closeActivity(RESULT_SERVER_NOT_LISTENING_ON_COMMAND_PORT);
        } else if (exception instanceof ConnectException && exception.getMessage().contains("ETIMEDOUT")) {
            // close the non-user-closeable connecting... dialog
            if (mConnectionProgressDialog != null)
                mConnectionProgressDialog.dismiss();

            closeActivity(RESULT_SERVER_CONNECTION_TIMED_OUT);
        }

        //TODO: wörkwörk markus
    }

    public void createButtons(final UpdateButtons addCom) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // clear all old buttons
                mRuntimeButtonLayout.removeAllViews();


                for (Map.Entry<Integer, String> button : addCom.buttons.entrySet()) {
                    Button btn = new Button(SendActivity.this);
                    btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, mRuntimeButtonLayout.getHeight() / addCom.buttons.size()));
                    mRuntimeButtonLayout.addView(btn);
                    btn.setText(button.getValue());
                    btn.setTag(button.getKey());
                    btn.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                mNetworkClient.sendCommand(new ButtonClick((Integer) view.getTag(), true));
                            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                                mNetworkClient.sendCommand(new ButtonClick((Integer) view.getTag(), false));
                            return true;
                        }
                    });
                }
            }
        });
    }

    /**
     * Tell PC to reposition the mouse cursor to the center of its main screen?
     * Probably shit code?
     * Arne plz help me
     *
     * @param view
     */
    public void repositionMouseCursor(View view) {
        //TODO: buttonID should be set without seeming so random but view.getTag() doesn't work :(
        mNetworkClient.sendCommand(new ButtonClick(2, false));
    }

    private void goToOptions() {
        Bundle in = getIntent().getExtras();

        Intent intent = new Intent(SendActivity.this, OptionsActivity.class);
        intent.putExtra(EXTRA_SERVER_PORT_COMMAND, in.getInt(EXTRA_SERVER_PORT_COMMAND));
        intent.putExtra(EXTRA_SERVER_ADDRESS, in.getString(EXTRA_SERVER_ADDRESS));
        SendActivity.this.startActivity(intent);
    }
}
