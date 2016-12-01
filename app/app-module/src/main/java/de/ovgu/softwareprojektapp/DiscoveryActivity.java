package de.ovgu.softwareprojektapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        // clear the server list of old entries
        onServerListUpdated(new LinkedList<NetworkDevice>());

        // only enable the discovery user interface if the discovery system is not running
        setDiscoveryUserInterfaceEnabled(!(mDiscovery != null && mDiscovery.isRunning()));

        // try to load old discovery configuration
        loadDiscoveryConfig();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // request code is ignored
        if (resultCode == SendActivity.RESULT_SERVER_REFUSED)
            UiUtil.showAlert(this, "Connection failed", "Server refused connection");
        else if (resultCode == SendActivity.RESULT_SERVER_NOT_LISTENING_ON_COMMAND_PORT)
            UiUtil.showAlert(this, "Connection failed", "Server seems to be offline");
        else if (resultCode == SendActivity.RESULT_SERVER_CONNECTION_TIMED_OUT)
            UiUtil.showAlert(this, "Connection failed", "The server connection has timed out! Check your network configuration.");
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
                    // parse info from user input
                    int discoveryPort = getDiscoveryPort();
                    String deviceName = getDeviceName();

                    // create discovery client
                    mDiscovery = new DiscoveryClient(
                            DiscoveryActivity.this,
                            DiscoveryActivity.this,
                            discoveryPort,
                            deviceName);
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

    /**
     * Switch all the widgets dis- or enabled regarding to the current state of discovery
     *
     * @param enable true if the discovery start ui should be enabled
     */
    private void setDiscoveryUserInterfaceEnabled(boolean enable) {
        // find the EditTexts for port and device name and set their state
        findViewById(R.id.port_edit_text).setEnabled(enable);
        findViewById(R.id.device_name_edit_text).setEnabled(enable);

        // if the discovery start ui is enabled, we are not currently scanning, so hide the progress
        // bar
        findViewById(R.id.discovery_progress_spinner).setVisibility(enable ? View.GONE : View.VISIBLE);

        // enable the discovery button if the ui should be enabled
        mStartDiscoveryButton.setEnabled(enable);
    }

    /**
     * Store the current discovery configuration persistently
     *
     * @param deviceName the device name to be stored
     * @param port the port to be stored
     */
    private void storeDiscoveryConfig(String deviceName, int port) {
        SharedPreferences sharedPref = getSharedPreferences("discovery_config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("port", port);
        editor.putString("deviceName", deviceName);
        editor.commit();
    }

    /**
     * Load the old discovery configuration, and apply to the edittexts
     */
    private void loadDiscoveryConfig() {
        SharedPreferences sharedPref = getSharedPreferences("discovery_config", Context.MODE_PRIVATE);
        int port = sharedPref.getInt("port", 8888);
        String deviceName = sharedPref.getString("deviceName", Build.MODEL);

        // find the EditTexts for port and device name and set their text
        ((EditText) findViewById(R.id.port_edit_text)).setText(String.valueOf(port));
        ((EditText) findViewById(R.id.device_name_edit_text)).setText(deviceName);
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

            // avoid non-existent ports
            if (port > 65535)
                throw new InvalidParameterException("Ports above 65535 do not exist");

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
    protected void onPause() {
        super.onPause();

        // store the discovery config
        EditText portEditText = (EditText) findViewById(R.id.port_edit_text);
        EditText deviceNameEditText = (EditText) findViewById(R.id.device_name_edit_text);
        storeDiscoveryConfig(deviceNameEditText.getText().toString(), Integer.valueOf(portEditText.getText().toString()));
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