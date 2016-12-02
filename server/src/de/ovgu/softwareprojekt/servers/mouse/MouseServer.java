package de.ovgu.softwareprojekt.servers.mouse;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.Server;
import de.ovgu.softwareprojekt.servers.filters.AverageMovementFilter;
import de.ovgu.softwareprojekt.servers.filters.MinimumAmplitudeFilter;

import java.awt.*;
import java.io.IOException;

/**
 * The MouseServer class is an exemplary extension of Server that enables
 * the user to move the mouse using his phone
 */
public class MouseServer extends Server {
    /**
     * Constant identifying left mouse button in app
     */
    static final int LEFT_MOUSE_BUTTON = 0;

    /**
     * Constant identifying right mouse button in app
     */
    static final int RIGHT_MOUSE_BUTTON = 1;

    /**
     * Constant identifying the resetPosToCenter option in app
     */
    static final int RESET_MOUSE_POSITION  = 2;

    /**
     * Mouse mover; endpoint for our data, button clicks etc.
     */
    private MouseMover mMouseMover;

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName if not null, this name will be used. otherwise, the devices hostname is used
     */
    public MouseServer(@Nullable String serverName) throws IOException, AWTException {
        super(serverName);

        // create a new mouse mover
        mMouseMover = new MouseMover();

        // this is how we currently define a filter pipeline:
        DataSink pipeline = new AverageMovementFilter(3, new MinimumAmplitudeFilter(mMouseMover, 1f));

        // register our mouse mover to receive gyroscope data
        registerDataSink(pipeline, SensorType.Gyroscope);

        // add left- and right click buttons
        addButton("left click", LEFT_MOUSE_BUTTON);
        addButton("right click", RIGHT_MOUSE_BUTTON);
    }

    @Override
    public void onException(Object o, Exception e, String s) {
        e.printStackTrace();
        System.out.println("with additional information:\n" + s);
        System.out.println("in " + o.getClass());
        System.exit(0);
    }

    /**
     * Called whenever a button is clicked
     * @param click event object specifying details like button id
     */
    @Override
    public void onButtonClick(ButtonClick click) {
        mMouseMover.click(click.mID, true);
        if (!click.isHold)
            mMouseMover.click(click.mID, false);
    }

    /**
     * Check whether a new client should be accepted
     * @param newClient the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    @Override
    public boolean acceptClient(NetworkDevice newClient) {
        System.out.println(newClient + " connected");
        return true;
    }

    /**
     * Called when a client sent a disconnect signal
     * @param disconnectedClient the lost client
     */
    @Override
    public void onClientDisconnected(NetworkDevice disconnectedClient) {
        System.out.println(disconnectedClient.name + " disconnected");
    }
}
