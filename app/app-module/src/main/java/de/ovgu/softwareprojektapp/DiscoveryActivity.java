package de.ovgu.softwareprojektapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.discovery.OnDiscoveryListener;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojektapp.networking.DiscoveryClient;

public class DiscoveryActivity extends AppCompatActivity implements OnDiscoveryListener, ExceptionListener {

    Button mStartDiscoveryButton;
    ListView mPossibleConnections;
    List<NetworkDevice> mServerList;

    /**
     * This discovery client enables us to find servers
     */
    DiscoveryClient mDiscovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        mStartDiscoveryButton = (Button) findViewById(R.id.startDiscovery);
        mStartDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // let this function handle
                startDiscovery();
            }
        });

        mPossibleConnections = (ListView) findViewById(R.id.possibleConnections);
        mPossibleConnections.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // store the information of the selected server in the intent extras
                Intent intent = new Intent(DiscoveryActivity.this, SendActivity.class);
                intent.putExtra(SendActivity.EXTRA_SERVER_NAME, mServerList.get(i).name);
                intent.putExtra(SendActivity.EXTRA_SERVER_ADDRESS, mServerList.get(i).address);
                intent.putExtra(SendActivity.EXTRA_SERVER_PORT_DISCOVERY, mServerList.get(i).discoveryPort);
                intent.putExtra(SendActivity.EXTRA_SERVER_PORT_COMMAND, mServerList.get(i).commandPort);
                intent.putExtra(SendActivity.EXTRA_SERVER_PORT_DATA, mServerList.get(i).dataPort);

                // store our name, too
                intent.putExtra(SendActivity.EXTRA_SELF_NAME, getDeviceName());

                // start activity; 2nd parameter unused by us
                DiscoveryActivity.this.startActivityForResult(intent, 0);

                // stop the discovery server
                mDiscovery.close();
            }
        });

        // default to the device model for identification
        ((EditText) findViewById(R.id.device_name_edit_text)).setText(Build.MODEL);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // clear the server list in case there is an old entry
        onServerListUpdated(new LinkedList<NetworkDevice>());

        // find the progress indicator
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.discovery_progress_spinner);

        // only enable the discovery user interface if the discovery system is not running
        setDiscoveryUserInterfaceEnabled(!(mDiscovery != null && mDiscovery.isRunning()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // request code is ignored
        if (resultCode == SendActivity.RESULT_SERVER_REFUSED)
            UiUtil.showAlert(this, "Connection failed", "Server refused connection");
        else if (resultCode == SendActivity.RESULT_SERVER_NOT_LISTENING_ON_COMMAND_PORT)
            UiUtil.showAlert(this, "Connection failed", "Server seems to be offline");
    }

    /**
     * Start (or restart) the discovery server gracefully
     */
    public void startDiscovery() {
        // only start new discovery if not running yet
        if (mDiscovery == null || !mDiscovery.isRunning()) {
            // create a new discovery thread, if this one already completed
            if (mDiscovery == null || mDiscovery.hasRun()) {
                try {
                    // parse port from input
                    int discoveryPort = getDiscoveryPort();

                    // create discovery client
                    mDiscovery = new DiscoveryClient(
                            DiscoveryActivity.this,
                            DiscoveryActivity.this,
                            discoveryPort,
                            getDeviceName());
                    // this is a network problem
                } catch (IOException e) {
                    onException(this, e, "DiscoveryActivity: could not create DiscoveryClient");
                }
                // these exceptions are thrown if the port is invalid, so we do not want to continue
                catch (NumberFormatException | InvalidParameterException e) {
                    return;
                }
            }

            setDiscoveryUserInterfaceEnabled(false);

            // start discovery
            mDiscovery.start();
        }
    }

    private void setDiscoveryUserInterfaceEnabled(boolean enable){
        // find the progress indicator
        View progressBar = findViewById(R.id.discovery_progress_spinner);

        // find the EditTexts for port and device name
        View portEditText = findViewById(R.id.port_edit_text);
        View deviceNameEditText = findViewById(R.id.device_name_edit_text);

        // refresh activation state
        portEditText.setEnabled(enable);
        deviceNameEditText.setEnabled(enable);

        if (!enable) {
            // show the progress indicator
            progressBar.setVisibility(View.VISIBLE);


        } else {
            // hide the progress indicator
            progressBar.setVisibility(View.GONE);

            // enable the discovery button in case we disabled it
            mStartDiscoveryButton.setEnabled(true);
        }

        // disable the discovery button, since we are still running
        mStartDiscoveryButton.setEnabled(enable);
    }

    /**
     * Parse the discovery port from the edittext for the discovery port
     *
     * @return 8888 if no port was entered, or the result port
     * @throws NumberFormatException     if a non-number was entered
     * @throws InvalidParameterException if a port below 1024 (reserved for root) was entered
     */
    private int getDiscoveryPort() throws NumberFormatException, InvalidParameterException {
        EditText portEditText = (EditText) findViewById(R.id.port_edit_text);
        String portFieldContent = portEditText.getText().toString();

        if (portFieldContent.length() == 0)
            return 8888;

        try {
            // try and convert the field content to a number
            int port = Integer.valueOf(portFieldContent);

            // avoid root-only ports
            if (port < 1024)
                throw new InvalidParameterException("Ports below 1024 are reserved");

            // remove error if we set one, since we have successfully survived the trials
            portEditText.setError(null);

            return port;
        } catch (NumberFormatException e) {
            // display an error about invalid numbers
            portEditText.setError("Numbers only");
            throw e;
        } catch (InvalidParameterException e) {
            // ports below 1024 are root ports...
            portEditText.setError(e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve the device name from source edittext
     */
    private String getDeviceName() {
        EditText deviceNameEditText = (EditText) findViewById(R.id.device_name_edit_text);
        return deviceNameEditText.getText().toString();
    }

    @Override
    public void onServerListUpdated(List<NetworkDevice> servers) {
        mServerList = servers;             //save server names and ips for further use

        final String[] stringNames = new String[servers.size()];


        for (int i = 0; i < servers.size(); i++) {
            stringNames[i] = servers.get(i).name;
        }

        // since this is called from a separate thread, we must use runOnUiThread to update the lits
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> serverNameAdapter = new ArrayAdapter<>(DiscoveryActivity.this, android.R.layout.simple_list_item_1, stringNames);
                mPossibleConnections.setAdapter(serverNameAdapter);
            }
        });
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        //TODO: wörkwörk markus
    }
}