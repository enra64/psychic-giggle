package de.ovgu.softwareprojektapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Map;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequestResponse;
import de.ovgu.softwareprojekt.control.commands.SetSensorCommand;
import de.ovgu.softwareprojekt.control.commands.UpdateButtons;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojektapp.networking.NetworkClient;
import de.ovgu.softwareprojektapp.sensors.SensorHandler;

public class SendActivity extends AppCompatActivity implements OnCommandListener, ExceptionListener {
    // de-magic-stringify the intent extra keys
    static final String EXTRA_SERVER_NAME = "Name";
    static final String EXTRA_SERVER_ADDRESS = "Address";
    static final String EXTRA_SERVER_PORT_DISCOVERY = "DiscoveryPort";
    static final String EXTRA_SERVER_PORT_COMMAND = "CommandPort";
    static final String EXTRA_SERVER_PORT_DATA = "DataPort";

    static final String EXTRA_SELF_NAME = "SelfName";

    static final int RESULT_SERVER_REFUSED = -1;
    static final int RESULT_USER_STOPPED = 0;
    static final int RESULT_SERVER_NOT_LISTENING_ON_COMMAND_PORT = -2;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mRuntimeButtonLayout = (LinearLayout) findViewById(R.id.linlay);

        // we create a NetworkDevice from our extras to have all the data we need in a neat package
        mNetworkClient = new NetworkClient(
                parseServer(),
                getIntent().getExtras().getString(EXTRA_SELF_NAME),
                this,
                this
        );

        // default to successful execution
        setResult(RESULT_OK);

        // create a new sensor handler to help with the sensors
        mSensorHandler = new SensorHandler(this);

        // immediately try to connect to the server
        initiateConnection();

        // default activity result is closing by user
        setResult(RESULT_USER_STOPPED);
    }

    /**
     * Create a NetworkDevice object from the intent extras
     */
    private NetworkDevice parseServer() {
        Bundle in = getIntent().getExtras();
        return new NetworkDevice(
                in.getString(EXTRA_SERVER_NAME),
                in.getInt(EXTRA_SERVER_PORT_DISCOVERY),
                in.getInt(EXTRA_SERVER_PORT_COMMAND),
                in.getInt(EXTRA_SERVER_PORT_DATA),
                in.getString(EXTRA_SERVER_ADDRESS));
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
     * This function is called by a button. It sends a command to the server which then closes the
     * connection. It also stops the activity.
     */
    public void endConnection(@Nullable View v) {
        // end connections
        mNetworkClient.signalConnectionEnd();

        // close activity
        closeActivity(RESULT_USER_STOPPED);
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
     * Enable or disable the gyroscope
     *
     * @param enable true if the gyroscope must be enabled
     */
    private void setSensor(final SensorType sensorType, boolean enable) {
        // configure the sensor run state
        if (!mSensorHandler.setRunning(mNetworkClient, sensorType, enable)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // show a warning if the requested sensor type does not exist
                    AlertDialog alertDialog = new AlertDialog.Builder(SendActivity.this).create();
                    alertDialog.setTitle("Sensor activation error");
                    alertDialog.setMessage("This device does not contain a " + sensorType.name());
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (DialogInterface.OnClickListener) null);
                    alertDialog.show();
                }
            });
        }
    }

    /**
     * Handle connection request responses
     *
     * @param granted true if the request was granted
     */
    private void handleConnectionResponse(boolean granted) {
        // the progress dialog can be dismissed in any case
        mConnectionProgressDialog.dismiss();

        if (granted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SendActivity.this, R.string.send_activity_connection_success, Toast.LENGTH_LONG).show();
                }
            });
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
    public void onCommand(InetAddress origin, Command command) {
        // decide what to do with the command
        switch (command.getCommandType()) {
            case ConnectionRequestResponse:
                ConnectionRequestResponse res = (ConnectionRequestResponse) command;
                handleConnectionResponse(res.grant);
                break;
            case SetSensor:
                // enable or disable a sensor
                SetSensorCommand com = (SetSensorCommand) command;
                setSensor(com.sensorType, com.enable);
                break;
            //adds a button to the activity with id and name
            case AddButton:
                UpdateButtons addCom = (UpdateButtons) command;
                //create button with name and id
                createButtons(addCom);
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
                    btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, mRuntimeButtonLayout.getHeight()/ addCom.buttons.size()));
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
}
