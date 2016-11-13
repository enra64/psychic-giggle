package de.ovgu.softwareprojektapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.net.InetAddress;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.CommandSource;
import de.ovgu.softwareprojekt.control.commands.BooleanCommand;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojektapp.networking.UdpConnection;
import de.ovgu.softwareprojektapp.sensors.Gyroscope;

public class SendActivity extends AppCompatActivity implements CommandSource.OnCommandListener {
    // de-magic-stringify the intent extra keys
    static final String EXTRA_SERVER_NAME = "Name";
    static final String EXTRA_SERVER_ADDRESS = "Address";
    static final String EXTRA_SERVER_PORT_DISCOVERY = "DiscoveryPort";
    static final String EXTRA_SERVER_PORT_COMMAND = "CommandPort";
    static final String EXTRA_SERVER_PORT_DATA = "DataPort";

    static final String EXTRA_SELF_NAME = "SelfName";

    /**
     * Stores necessary information about our server, like data port, command port, and its address
     */
    private NetworkDevice mServer;

    /**
     *
     */
    private CommandConnection mCommandConnection;

    private UdpConnection mOutboundDataConnection;

    private Gyroscope mGyroscope = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        // populates mServer
        parseServer();

        // create a command connection
        try {
            mCommandConnection = new CommandConnection();
            mCommandConnection.setCommandListener(this);
            mCommandConnection.setRemote(mServer.getInetAddress(), mServer.commandPort);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }


        try {
            mOutboundDataConnection = new UdpConnection(mServer.getInetAddress(), mServer.dataPort);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }
    }

    /**
     * Create a NetworkDevice object from the intent extras
     */
    private void parseServer() {
        Bundle in = getIntent().getExtras();
        mServer = new NetworkDevice(
                in.getString(EXTRA_SERVER_NAME),
                in.getInt(EXTRA_SERVER_PORT_DISCOVERY),
                in.getInt(EXTRA_SERVER_PORT_COMMAND),
                in.getInt(EXTRA_SERVER_PORT_DATA),
                in.getString(EXTRA_SERVER_ADDRESS));
    }

    private NetworkDevice getSelf() {
        return new NetworkDevice(
                getIntent().getExtras().getString(EXTRA_SELF_NAME),
                mCommandConnection.getLocalPort(),
                mOutboundDataConnection.getLocalPort()
        );
    }

    public void initiateConnection(View v) {
        try {
            // begin listening for commands
            mCommandConnection.start();

            // send a connection request
            new SendCommand().execute(new ConnectionRequest(getSelf()));

        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }
    }

    private void setGyroscope(boolean enable) {
        if (enable) {
            if (mGyroscope == null)
                mGyroscope = new Gyroscope(this);

            mGyroscope.setDataSink(mOutboundDataConnection);
            mGyroscope.start();
        } else {
            mGyroscope.close();
        }
    }

    @Override
    public void onCommand(InetAddress origin, Command command) {
        switch (command.getCommandType()) {
            case SetGyroscope:
                setGyroscope(((BooleanCommand) command).value);
                break;
            // ignore unhandled commands
            default:
                break;
        }
    }

    private class SendCommand extends AsyncTask<Command, Void, Void> {
        @Override
        protected Void doInBackground(Command... commands) {
            try {
                mCommandConnection.inputCommand(commands[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}