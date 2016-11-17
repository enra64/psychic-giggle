package de.ovgu.softwareprojektapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.IOException;
import java.net.InetAddress;

import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.AddButton;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.CommandType;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequestResponse;
import de.ovgu.softwareprojekt.control.commands.EndConnection;
import de.ovgu.softwareprojekt.control.commands.SetSensorCommand;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojektapp.networking.NetworkClient;
import de.ovgu.softwareprojektapp.sensors.Gyroscope;

import static de.ovgu.softwareprojekt.control.commands.CommandType.AddButton;
import static de.ovgu.softwareprojekt.control.commands.CommandType.SetSensor;

public class SendActivity extends AppCompatActivity implements OnCommandListener, ExceptionListener {
    // de-magic-stringify the intent extra keys
    static final String EXTRA_SERVER_NAME = "Name";
    static final String EXTRA_SERVER_ADDRESS = "Address";
    static final String EXTRA_SERVER_PORT_DISCOVERY = "DiscoveryPort";
    static final String EXTRA_SERVER_PORT_COMMAND = "CommandPort";
    static final String EXTRA_SERVER_PORT_DATA = "DataPort";

    static final String EXTRA_SELF_NAME = "SelfName";

    /**
     * Gyroscope sensor object; implements datasource, listens to gyroscope changes
     */
    private Gyroscope mGyroscope = null;

    /**
     * The network client organises all our communication with the server
     */
    NetworkClient mNetworkClient;

    /**
     * linear layout as a space to add buttons
     */
    private LinearLayout ll = (LinearLayout) findViewById(R.id.linlay);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        // we create a NetworkDevice from our extras to have all the data we need in a neat package
        mNetworkClient = new NetworkClient(
                parseServer(),
                getIntent().getExtras().getString(EXTRA_SELF_NAME),
                this,
                this
        );

        // prepare the gyroscope
        mGyroscope = new Gyroscope(this);

        // make the gyroscope directly output to the network
        mGyroscope.setDataSink(mNetworkClient);
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
    public void initiateConnection(View v) {
        // begin listening for commands
        mNetworkClient.requestConnection();
    }

    /**
     * This function is called by a button. It sends a command to the server which then closes the
     * connection. It also stops the activity.
     */
    public void endConnection(View v){
        mNetworkClient.endConnection();
        super.onBackPressed();
    }

    /**
     * Enable or disable the gyroscope
     * @param enable true if the gyroscope must be enabled
     */
    private void setGyroscope(boolean enable) {
        // configure the gyroscope run state
        mGyroscope.setRunning(enable);
    }

    /**
     * Called whenever a new command comes in
     *
     * @param origin  where the command comes from
     * @param command the command itself
     */
    @Override
    public void onCommand(InetAddress origin, Command command) {
        // decide what to do with the command
        switch (command.getCommandType()) {
            case ConnectionRequestResponse:
                ConnectionRequestResponse res = (ConnectionRequestResponse) command;
                // res.grant is true if the connection was allowed
                // TODO: display connection success
            case SetSensor:
                // enable or disable a sensor
                SetSensorCommand com = (SetSensorCommand) command;

                // decide which sensor should be switched
                switch (com.sensorType) {
                    case Gyroscope:
                        setGyroscope(com.enable);
                        break;
                    default:
                        break;
                }

                break;

            //adds a button to the activity with id and name
            case AddButton:
                AddButton addCom = (AddButton) command;
                //create button with name and id
                createNewButton(addCom);

                break;



            // ignore unhandled commands
            default:
                break;
        }
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        //TODO: wörkwörk markus
    }

    public void createNewButton(AddButton addCom){
        Button btn = new Button(this);
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        ll.addView(btn);
        btn.setText(addCom.mName);
        btn.setTag((Integer) addCom.mID);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send ButtonClick command with button id per networkclient
                mNetworkClient.sendCommand(new ButtonClick((Integer) view.getTag()));
            }
        });
    }
}
