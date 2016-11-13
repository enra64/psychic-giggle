package de.ovgu.softwareprojektapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.CommandSource;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

public class SendActivity extends AppCompatActivity implements CommandSource.OnCommandListener {
    // de-magic-stringify the intent extra keys
    static final String EXTRA_SERVER_NAME = "Name";
    static final String EXTRA_SERVER_ADDRESS = "Address";
    static final String EXTRA_SERVER_PORT_DISCOVERY = "DiscoveryPort";
    static final String EXTRA_SERVER_PORT_COMMAND = "CommandPort";
    static final String EXTRA_SERVER_PORT_DATA = "DataPort";

    /**
     * Stores necessary information about our server, like data port, command port, and its address
     */
    private NetworkDevice mServer;

    /**
     *
     */
    private CommandConnection mCommandConnection;



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
    }

    /**
     * Create a NetworkDevice object from the intent extras
     */
    private void parseServer(){
        Bundle in = getIntent().getExtras();
        mServer = new NetworkDevice(
                in.getString(EXTRA_SERVER_NAME),
                in.getInt(EXTRA_SERVER_PORT_DISCOVERY),
                in.getInt(EXTRA_SERVER_PORT_COMMAND),
                in.getInt(EXTRA_SERVER_PORT_DATA),
                in.getString(EXTRA_SERVER_ADDRESS));
    }

    public void initiateConnection(View v){
        try {
            // begin listening for commands
            mCommandConnection.start();

            // send a connection request
            new SendCommand().execute(new ConnectionRequest());

        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exception handling
        }
    }

    @Override
    public void onCommand(Command command) {

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