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

import de.ovgu.softwareprojektapp.server_discovery.Discovery;
import de.ovgu.softwareprojektapp.server_discovery.OnDiscoveryListener;
import de.ovgu.softwareprojektapp.server_discovery.Server;

public class DiscoveryActivity extends AppCompatActivity implements OnDiscoveryListener {

    Button mStartDiscovery;
    ListView mPossibleConnections;
    List<Server> mServerList;
    Discovery mDiscovery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        mStartDiscovery = (Button) findViewById(R.id.startDiscovery);
        mStartDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscovery.startDiscovery(DiscoveryActivity.this);
            }
        });

        mPossibleConnections = (ListView) findViewById(R.id.possibleConnections);

        mPossibleConnections.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(DiscoveryActivity.this, SendActivity.class);
                intent.putExtra("Name", mServerList.get(i).name);
                intent.putExtra("Address" , mServerList.get(i).address);
                intent.putExtra("Port", mServerList.get(i).port);

                DiscoveryActivity.this.startActivity(intent);
            }
        });

    }

    @Override
    public void onServerListUpdated(List<Server> servers) {

        mServerList = servers;             //save server names and ips for further use

        String[] stringNames = new String[servers.size()];


        for(int i = 0; i < servers.size(); i++){
            stringNames[i] = servers.get(i).name;

        }

        ArrayAdapter<String> serverNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringNames);
        mPossibleConnections.setAdapter(serverNameAdapter);
    }
}