package de.ovgu.softwareprojekt.servers;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.callback_interfaces.ClientListener;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.networking.Server;

/**
 * Created by markus on 25.01.17.
 */
public class ExampleServer implements NetworkDataSink, ClientListener {
    public ExampleServer(){
        Server server = new Server();
        server.setClientListener(this);
    }
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean acceptClient(NetworkDevice newClient) {
        return false;
    }

    @Override
    public void onClientDisconnected(NetworkDevice disconnectedClient) {

    }

    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {

    }

    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {

    }
}
