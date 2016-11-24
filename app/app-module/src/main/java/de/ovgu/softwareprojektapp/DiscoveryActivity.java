package de.ovgu.softwareprojektapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.IOException;
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

    /**
     * how we announce ourselves to servers
     */
    private static final String NAME = "bond. _james_ bond";

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
                mStartDiscoveryButton.setEnabled(false);
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
                intent.putExtra(SendActivity.EXTRA_SELF_NAME, NAME);

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

        // clear the server list in case there is an old entry
        onServerListUpdated(new LinkedList<NetworkDevice>());

        // hide the progress bar for now
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.discovery_progress_spinner);
        progressBar.setVisibility(View.GONE);

        // enable the discovery button in case we disabled it
        mStartDiscoveryButton.setEnabled(true);
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
                    mDiscovery = new DiscoveryClient(
                            DiscoveryActivity.this,
                            DiscoveryActivity.this,
                            8888,
                            NAME);
                } catch (IOException e) {
                    onException(this, e, "DiscoveryActivity: could not create DiscoveryClient");
                }
            }

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.discovery_progress_spinner);
            progressBar.setVisibility(View.VISIBLE);

            // start discovery
            mDiscovery.start();
        }
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