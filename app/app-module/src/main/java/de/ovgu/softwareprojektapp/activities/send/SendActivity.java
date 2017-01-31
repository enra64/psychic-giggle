package de.ovgu.softwareprojektapp.activities.send;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequestResponse;
import de.ovgu.softwareprojekt.control.commands.ChangeSensorSensitivity;
import de.ovgu.softwareprojekt.control.commands.ResetToCenter;
import de.ovgu.softwareprojekt.control.commands.SensorDescription;
import de.ovgu.softwareprojekt.control.commands.SensorRangeNotification;
import de.ovgu.softwareprojekt.control.commands.DisplayNotification;
import de.ovgu.softwareprojekt.control.commands.SetSensorCommand;
import de.ovgu.softwareprojekt.control.commands.SetSensorSpeed;
import de.ovgu.softwareprojekt.control.commands.UpdateButtonsMap;
import de.ovgu.softwareprojekt.control.commands.UpdateButtonsXML;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojektapp.R;
import de.ovgu.softwareprojektapp.UiUtil;
import de.ovgu.softwareprojektapp.activities.DiscoveryActivity;
import de.ovgu.softwareprojektapp.activities.OptionsActivity;
import de.ovgu.softwareprojektapp.networking.NetworkClient;
import de.ovgu.softwareprojektapp.sensors.SensorHandler;
import de.ovgu.softwareprojektapp.sensors.SensorNotFoundException;

public class SendActivity extends AppCompatActivity implements OnCommandListener, ExceptionListener, NetworkClient.TimeoutListener {
    /**
     * The intent extras (the data given to us by the {@link DiscoveryActivity})
     */
    public static final String
            EXTRA_SERVER_NAME = "Name",
            EXTRA_SERVER_ADDRESS = "Address",
            EXTRA_SERVER_PORT_DISCOVERY = "DiscoveryPort",
            EXTRA_SERVER_PORT_COMMAND = "CommandPort",
            EXTRA_SERVER_PORT_DATA = "DataPort",
            EXTRA_SELF_NAME = "SelfName",
            EXTRA_ACTIVE_SENSORS = "ActiveSensor",
            EXTRA_SENSOR_DESCRIPTIONS = "SensorDescription";

    /**
     * result codes for the activity
     * result codes for the activity
     */
    public static final int
            RESULT_SERVER_REFUSED = -1,
            RESULT_USER_STOPPED = 0,
            RESULT_SERVER_NOT_LISTENING_ON_PORT = -2,
            RESULT_SERVER_CONNECTION_TIMED_OUT = -3,
            RESULT_SERVER_NOT_RESPONDING_TO_REQUEST = -4;

    /**
     * The network client organises all our communication with the server
     */
    NetworkClient mNetworkClient;

    /**
     * linear layout as a space to add buttons
     */
    private LayoutParser mRuntimeButtonLayout;

    /**
     * The SensorHandler used to handle our sensor needs
     */
    SensorHandler mSensorHandler;

    /**
     * Dialog for showing connection progress
     */
    ProgressDialog mConnectionProgressDialog;

    /**
     * This timer is for timing out the server response
     */
    private Timer mConnectionResponseTimeoutTimer;

    /**
     * handle the shown Notifications
     */
    private NotificationManager mNotificationManager;

    /**
     * saves descriptions for the used sensors
     */
    private HashMap<SensorType, String> sensorDescriptions = new HashMap<SensorType, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        // find the layout we use to display buttons requested by the server
        mRuntimeButtonLayout = (LayoutParser) findViewById(R.id.runtime_button_parent);

        // we create a NetworkDevice from our extras to have all the data we need in a neat package
        parseIncomingNetworkDevice();


        // create a new sensor handler to help with the sensors
        mSensorHandler = new SensorHandler(this, mNetworkClient);

        // disable the sensors while this button is held down
        CheckBox disableSensorsCheck = (CheckBox) findViewById(R.id.holdSensor);
        disableSensorsCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mSensorHandler.temporarilySetSensors(!isChecked);
            }
        });

        // default to successful execution
        setResult(RESULT_USER_STOPPED);

        // immediately try to connect to the server
        initiateConnection();

        // set NetworkClient in LayoutParser
        mRuntimeButtonLayout.setNetworkClient(mNetworkClient);

        // set Notificationmanager in order to handle the notifications
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Connection timeout callback
     */
    @Override
    public void onConnectionTimeout() {
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

        mConnectionResponseTimeoutTimer = new Timer();
        mConnectionResponseTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                closeActivity(RESULT_SERVER_NOT_RESPONDING_TO_REQUEST);
            }
        }, 3000);

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

        // close connection dialog if it was still open
        if(mConnectionProgressDialog != null)
            mConnectionProgressDialog.cancel();

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
        try {
            mSensorHandler.setRunning(requiredSensors);
        } catch (SensorNotFoundException e) {
            UiUtil.showAlert(this, "Sensor activation error", "This device does not contain a required sensor: " + e.getMissingSensor().name());
        }
    }

    /**
     * Suspend sensors, cancel connection response timeout timer
     */
    @Override
    protected void onPause() {
        super.onPause();
        mSensorHandler.suspendSensors();
        mConnectionResponseTimeoutTimer.cancel();
    }

    /**
     * Wake up sensors and refresh sensor range and sensitivity
     */
    @Override
    protected void onResume() {
        super.onResume();

        // wake up the sensors again
        mSensorHandler.wakeUpSensors();

        // refresh sensor sensitivity settings and sensor ranges
        SharedPreferences sensitivitySettings = getSharedPreferences(OptionsActivity.SENSITIVITY_PREFS_NAME, 0);
        for (SensorType s : SensorType.values()) {
            try {
                mNetworkClient.sendCommand(new ChangeSensorSensitivity(s, sensitivitySettings.getInt(s.toString(), 50)));
                mNetworkClient.sendCommand(new SensorRangeNotification(s, mSensorHandler.getRange(s)));
            } catch (SensorNotFoundException e) {
                onException(this, e, "Could not get sensor range");
            }
        }

    }

    /**
     * close connection, remove all notifications
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mNotificationManager != null)
            mNotificationManager.cancelAll();

        mNetworkClient.signalConnectionEnd();
        mNetworkClient.close();
    }

    /**
     * Handle connection request responses
     *
     * @param granted true if the request was granted
     */
    private void handleConnectionResponse(boolean granted) {
        // the response timeout is useless now
        mConnectionResponseTimeoutTimer.cancel();

        // the progress dialog can be dismissed in any case
        mConnectionProgressDialog.dismiss();

        if (granted) {
            UiUtil.showToast(this, getString(R.string.send_activity_connection_success));

        } else
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
            case UpdateButtonsMap:
                //create button with name and id
                UpdateButtonsMap addComMap = (UpdateButtonsMap) command;
                createButtons(addComMap);
                break;
            case UpdateButtonsXML:
                UpdateButtonsXML addComXML = (UpdateButtonsXML) command;
                createButtons(addComXML);
                break;
            case SetSensorSpeed:
                // update the speed for the given sensor
                SetSensorSpeed setSpeedCommand = (SetSensorSpeed) command;
                mSensorHandler.setSensorSpeed(setSpeedCommand.affectedSensor, setSpeedCommand.sensorSpeed);
                break;
            case DisplayNotification:
                //display message
                DisplayNotification notification = (DisplayNotification) command;
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(notification.title)
                        .setContentText(notification.content)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL);
                if (Build.VERSION.SDK_INT >= 21)
                    mBuilder.setVibrate(new long[0]);
                mNotificationManager.notify(notification.id, mBuilder.build());
                break;
            //put sensor descriptions in an EnumMap for each SensorType
            case SensorDescription:
                SensorDescription setSensorDescription = (SensorDescription) command;
                sensorDescriptions.put(setSensorDescription.usedSensor, setSensorDescription.sensorDescription);
                break;
            // ignore unhandled commands
            default:
                break;
        }
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        exception.printStackTrace();

        // close the non-user-closeable connecting... dialog
        if (mConnectionProgressDialog != null)
            mConnectionProgressDialog.dismiss();

        // this exception is thrown when we send commands, but the server has closed its command port
        if (exception instanceof ConnectException && (exception.getMessage().contains("ECONNREFUSED") || exception.getMessage().contains("Connection refused")))
            closeActivity(RESULT_SERVER_NOT_LISTENING_ON_PORT);
        else if (exception instanceof ConnectException && exception.getMessage().contains("ETIMEDOUT"))
            closeActivity(RESULT_SERVER_CONNECTION_TIMED_OUT);
        else if (exception instanceof ConnectException && exception.getMessage().contains("Socket is closed"))
            closeActivity(RESULT_SERVER_NOT_LISTENING_ON_PORT);
        else if (exception instanceof SocketException && info.contains("could not send SensorData object"))
            closeActivity(RESULT_SERVER_NOT_LISTENING_ON_PORT);
        else {
            Log.w("spapp", "UNHANDLED EXCEPTION SENDACTIVITY:");
            exception.printStackTrace();
        }
    }

    /**
     * Create buttons from a mapping from button ids to button texts
     *
     * @param addCom the command that was sent to change the button display
     */
    private void createButtons(final UpdateButtonsMap addCom) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRuntimeButtonLayout.createFromMap(SendActivity.this, addCom.buttons);
            }
        });
    }

    /**
     * Create buttons from a xml string
     *
     * @param addCom the command that was sent to change the button display
     */
    private void createButtons(final UpdateButtonsXML addCom) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mRuntimeButtonLayout.createFromXML(addCom.xmlContent);
                } catch (InvalidLayoutException e) {
                    UiUtil.showAlert(SendActivity.this, "invalid server configuration", "XML button layout could not be parsed");
                }
            }
        });
    }

    /**
     * Tell PC to reposition the mouse cursor to the center of its main screen
     *
     * @param view ignored
     */
    public void onReset(@Nullable View view) {
        mNetworkClient.sendCommand(new ResetToCenter());
    }

    /**
     * Open the {@link OptionsActivity}
     */
    private void goToOptions() {
        Bundle in = getIntent().getExtras();


        EnumMap<SensorType, Boolean> sensorActivationMap = new EnumMap<>(SensorType.class);

        for(SensorType sensor : SensorType.values())
            sensorActivationMap.put(sensor, mSensorHandler.isRegistered(sensor));

        Intent intent = new Intent(SendActivity.this, OptionsActivity.class);
        intent.putExtra(EXTRA_SERVER_PORT_COMMAND, in.getInt(EXTRA_SERVER_PORT_COMMAND));
        intent.putExtra(EXTRA_SERVER_ADDRESS, in.getString(EXTRA_SERVER_ADDRESS));

        intent.putExtra(EXTRA_ACTIVE_SENSORS, sensorActivationMap);
        intent.putExtra(EXTRA_SENSOR_DESCRIPTIONS, sensorDescriptions);
        SendActivity.this.startActivity(intent);
    }
}
