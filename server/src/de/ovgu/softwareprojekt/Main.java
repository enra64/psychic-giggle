package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojekt.networking.ClientListener;
import de.ovgu.softwareprojekt.networking.Server;

import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {
        // create a new server, and handle its exceptions by dying
        Server server = new Server(new ExceptionListener() {
            @Override
            public void onException(Object o, Exception e, String s) {
                e.printStackTrace();
                System.out.println("with additional information:\n" + s);
                System.out.println("in " + o.getClass());
                System.exit(0);
            }
        },
                new ClientListener() {
                    @Override
                    public boolean acceptClient(NetworkDevice newClient) {
                        // default accept clients
                        return true;
                    }

                    @Override
                    public void onClientDisconnected(NetworkDevice disconnectedClient) {
                        System.out.println(disconnectedClient.name + " disconnected");
                    }
                });

        // register a new mouse move to be used as data sink for the gyroscope
        server.registerDataSink(new MouseMover(), SensorType.Gyroscope);
    }
}
