package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;
import de.ovgu.softwareprojekt.networking.ButtonListener;
import de.ovgu.softwareprojekt.networking.ClientListener;
import de.ovgu.softwareprojekt.networking.Server;

import java.io.IOException;


public class Main {

    /**
     * Constants to identify which AppButton was activated;
     */
    public static final int LEFTMOUSECLICK = 0;
    public static final int RIGHTMOUSECLICK = 1;
    // public static final int STOPMOVEMENT = 2; not needed?

    public static void main(String[] args) throws IOException {
        final MouseMover mover = new MouseMover();
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
                },
                new ButtonListener() {
                    @Override
                    public void onButtonClick(ButtonClick click) {
                        mover.click(click.mID, true);
                        if (!click.isHold)
                            mover.click(click.mID, false);
                    }
                });

        // register a new mouse move to be used as data sink for the gyroscope
        server.registerDataSink(mover, SensorType.Gyroscope);
        server.addButton("left click", LEFTMOUSECLICK);
        server.addButton("right click", RIGHTMOUSECLICK);
        // server.addButton("stop movement", STOPMOVEMENT); not needed for now?
    }
}
