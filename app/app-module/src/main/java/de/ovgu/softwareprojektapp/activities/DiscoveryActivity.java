package de.ovgu.softwareprojektapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.discovery.OnDiscoveryListener;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojektapp.R;
import de.ovgu.softwareprojektapp.activities.send.SendActivity;
import de.ovgu.softwareprojektapp.UiUtil;
import de.ovgu.softwareprojektapp.networking.DiscoveryClient;

import static de.ovgu.softwareprojektapp.activities.send.SendActivity.RESULT_SERVER_CONNECTION_TIMED_OUT;
import static de.ovgu.softwareprojektapp.activities.send.SendActivity.RESULT_SERVER_NOT_LISTENING_ON_PORT;
import static de.ovgu.softwareprojektapp.activities.send.SendActivity.RESULT_SERVER_NOT_RESPONDING_TO_REQUEST;
import static de.ovgu.softwareprojektapp.activities.send.SendActivity.RESULT_SERVER_REFUSED;

public class DiscoveryActivity extends AppCompatActivity implements OnDiscoveryListener, ExceptionListener {
    /**
     * The button we use for discovery
     */
    Button mStartDiscoveryButton;

    /**
     * The ListView we use to display available servers
     */
    ListView mPossibleConnections;

    /**
     * List of current servers
     */
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
                // either start or stop the discovery
                if (mDiscovery != null && mDiscovery.isRunning())
                    stopDiscovery();
                else
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDiscovery();
    }

    /**
     * This function is called by a button in the action bar. It sends a command to the server which
     * then closes the connection. It also stops the activity.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings)
            startActivity(new Intent(this, OptionsActivity.class));
        // invoke the superclass if we didn't want to handle the click
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add all requested buttons to the action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_discovery, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_SERVER_REFUSED:
                UiUtil.showAlert(this, "Connection failed", "Server refused connection");
                break;
            case RESULT_SERVER_NOT_LISTENING_ON_PORT:
                UiUtil.showAlert(this, "Connection failed", "Server seems to be offline");
                break;
            case RESULT_SERVER_CONNECTION_TIMED_OUT:
                UiUtil.showAlert(this, "Connection failed", "The server connection has timed out! Check your network configuration.");
                break;
            case RESULT_SERVER_NOT_RESPONDING_TO_REQUEST:
                UiUtil.showAlert(this, "Connection failed", "The server did not respond to the connection request in time. Please try again!");
        }
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
     * Stop discovering
     */
    private void stopDiscovery() {
        if (mDiscovery != null)
            mDiscovery.close();
        setDiscoveryUserInterfaceEnabled(true);
    }

    private int getDiscoveryPort() {
        SharedPreferences sharedPref = getSharedPreferences(OptionsActivity.DISCOVERY_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPref.getInt(OptionsActivity.DISCOVERY_PREFS_PORT, 8888);
    }

    private String getDeviceName() {
        SharedPreferences sharedPref = getSharedPreferences(OptionsActivity.DISCOVERY_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(OptionsActivity.DISCOVERY_PREFS_DEVICE_NAME, Build.MODEL);
    }

    /**
     * Switch all the widgets dis- or enabled regarding to the current state of discovery
     *
     * @param enable true if the discovery start ui should be enabled
     */
    private void setDiscoveryUserInterfaceEnabled(boolean enable) {
        // if the discovery start ui is enabled, we are not currently scanning, so hide the progress
        // bar
        findViewById(R.id.discovery_progress_spinner).setVisibility(enable ? View.GONE : View.VISIBLE);

        // set start/stop discovery text
        mStartDiscoveryButton.setText(getString(enable ? R.string.start_discovery : R.string.stop_discovery));
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

        Log.w(origin.toString(), info, exception);
        exception.printStackTrace();

    }
}