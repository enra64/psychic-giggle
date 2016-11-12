package de.ovgu.softwareprojektapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.net.SocketException;
import java.util.List;

import de.ovgu.softwareprojekt.discovery.DeviceIdentification;
import de.ovgu.softwareprojekt.discovery.OnDiscoveryListener;
import de.ovgu.softwareprojektapp.server_discovery.DiscoveryThread;

public class DiscoveryActivity extends AppCompatActivity implements OnDiscoveryListener {

    Button mStartDiscovery;
    ListView mPossibleConnections;
    List<DeviceIdentification> mServerList;
    DiscoveryThread mDiscovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        // initialise discovery thread with listener, remote port, local port, name
        try {
            mDiscovery = new DiscoveryThread(this, 8888, "bond. _james_ bond");
        } catch (SocketException e) {
            e.printStackTrace();
        }

        mStartDiscovery = (Button) findViewById(R.id.startDiscovery);
        mStartDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start discovering
                mDiscovery.start();
            }
        });

        mPossibleConnections = (ListView) findViewById(R.id.possibleConnections);

        mPossibleConnections.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(DiscoveryActivity.this, SendActivity.class);
                intent.putExtra("Name", mServerList.get(i).name);
                intent.putExtra("Address", mServerList.get(i).address);
                intent.putExtra("Port", mServerList.get(i).port);

                DiscoveryActivity.this.startActivity(intent);
            }
        });

    }

    @Override
    public void onServerListUpdated(List<DeviceIdentification> servers) {
        mServerList = servers;             //save server names and ips for further use

        final String[] stringNames = new String[servers.size()];


        for (int i = 0; i < servers.size(); i++) {
            stringNames[i] = servers.get(i).name;

        }




        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> serverNameAdapter = new ArrayAdapter<String>(DiscoveryActivity.this, android.R.layout.simple_list_item_1, stringNames);
                mPossibleConnections.setAdapter(serverNameAdapter);
            }
        });
    }
}