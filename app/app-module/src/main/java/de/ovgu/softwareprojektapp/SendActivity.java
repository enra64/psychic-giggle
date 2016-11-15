package de.ovgu.softwareprojektapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.net.InetAddress;

import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.CommandType;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequestResponse;
import de.ovgu.softwareprojekt.control.commands.EndConnection;
import de.ovgu.softwareprojekt.control.commands.SetSensorCommand;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojektapp.networking.NetworkClient;
import de.ovgu.softwareprojektapp.sensors.Gyroscope;

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
     * Enable or disable the gyroscope
     * @param enable true if the gyroscope must be enabled
     */
    private void setGyroscope(boolean enable) {
        if (enable) {
            // create a new gyroscope if that has not yet happened
            if (mGyroscope == null)
                mGyroscope = new Gyroscope(this);

            // make the gyroscope directly output to the network
            mGyroscope.setDataSink(mNetworkClient);

            // start listening to gyroscope events
            mGyroscope.start();

        } else {
            // disable the gyroscope
            mGyroscope.close();
            mGyroscope = null;
        }
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
                // res.grant is true if the connection was
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
            // ignore unhandled commands
            default:
                break;
        }
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        //TODO: wörkwörk markus
    }
}
