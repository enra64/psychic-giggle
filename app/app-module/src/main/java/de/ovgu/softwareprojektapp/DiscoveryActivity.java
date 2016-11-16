package de.ovgu.softwareprojektapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.discovery.OnDiscoveryListener;
import de.ovgu.softwareprojektapp.networking.DiscoveryClient;

public class DiscoveryActivity extends AppCompatActivity implements OnDiscoveryListener {

    Button mStartDiscovery;
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

        // initialise discovery thread with listener, remote discovery listening port, and our name
        mDiscovery = new DiscoveryClient(this, 8888, NAME);

        mStartDiscovery = (Button) findViewById(R.id.startDiscovery);
        mStartDiscovery.setOnClickListener(new View.OnClickListener() {
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
                intent.putExtra(SendActivity.EXTRA_SELF_NAME, NAME);

                DiscoveryActivity.this.startActivity(intent);

                // stop the discovery server
                mDiscovery.close();
            }
        });
    }

    /**
     * Start (or restart) the discovery server gracefully
     */
    public void startDiscovery() {
        // only start new discovery if not running yet
        if (!mDiscovery.isRunning()) {
            // create a new discovery thread, if this one already completed
            if (mDiscovery.hasRun())
                mDiscovery = new DiscoveryClient(DiscoveryActivity.this, 8888, NAME);

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
                ArrayAdapter<String> serverNameAdapter = new ArrayAdapter<String>(DiscoveryActivity.this, android.R.layout.simple_list_item_1, stringNames);
                mPossibleConnections.setAdapter(serverNameAdapter);
            }
        });
    }
}