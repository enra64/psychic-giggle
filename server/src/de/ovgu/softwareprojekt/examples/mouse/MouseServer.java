package de.ovgu.softwareprojekt.examples.mouse;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.AbstractPsychicServer;
import de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder;
import de.ovgu.softwareprojekt.pipeline.filters.DifferenceThresholdFilter;
import de.ovgu.softwareprojekt.pipeline.filters.AveragingFilter;
import de.ovgu.softwareprojekt.pipeline.filters.ScalingFilter;
import de.ovgu.softwareprojekt.pipeline.filters.UserSensitivityMultiplicator;

import java.awt.*;
import java.io.IOException;

/**
 * The MouseServer class is an exemplary extension of AbstractPsychicServer that enables
 * the user to move the mouse using his phone
 */
public class MouseServer extends AbstractPsychicServer {
    /**
     * Constant identifying left mouse button in app
     */
    static final int LEFT_MOUSE_BUTTON = 0;

    /**
     * Constant identifying right mouse button in app
     */
    static final int RIGHT_MOUSE_BUTTON = 1;

    /**
     * Mouse mover; endpoint for our data, button clicks etc.
     */
    private MouseMover mMouseMover;

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName if not null, this name will be used. otherwise, the devices hostname is used
     * @throws IOException  if an issue with the phone communication arose
     * @throws AWTException if your device does not support javas {@link Robot} input
     */
    public MouseServer(@Nullable String serverName) throws IOException, AWTException {
        super(serverName);

        // create a new mouse mover
        mMouseMover = new MouseMover();

        // this is how we define a filter pipeline:
        FilterPipelineBuilder builder = new FilterPipelineBuilder();
        builder.append(new ScalingFilter(20, 5));
        builder.append(new UserSensitivityMultiplicator());
        builder.append(new AveragingFilter(3));
        builder.append(new DifferenceThresholdFilter(.1f));

        // register our mouse mover to receive gyroscope data
        registerDataSink(builder.build(mMouseMover), SensorType.Gyroscope);

        // add left- and right click buttons
        addButton("left click", LEFT_MOUSE_BUTTON);
        addButton("right click", RIGHT_MOUSE_BUTTON);

        // set a maximum client count
        setClientMaximum(1);
    }

    /**
     * Handle exceptions by printing them and then closing down
     */
    @Override
    public void onException(Object origin, Exception e, String info) {
        System.out.println("Exception in " + origin.getClass() + "with additional information:\n" + info);
        System.out.flush();
        e.printStackTrace();
        System.exit(1);
    }

    /**
     * Called whenever a button is clicked
     *
     * @param click  event object specifying details like button id
     * @param origin the client that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        mMouseMover.click(click.getId(), click.isPressed());
    }

    /**
     * Check whether a new client should be accepted
     *
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
     *
     * @param disconnectedClient the lost client
     */
    @Override
    public void onClientDisconnected(NetworkDevice disconnectedClient) {
        System.out.println(disconnectedClient.name + " disconnected");
    }

    /**
     * called when a Client is successfully connected
     *
     * @param timeoutClient the client that connected successfully
     */
    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {
        System.out.println(timeoutClient.name + " had a timeout");
    }

    /**
     * called when a Client is successfully connected
     *
     * @param connectedClient the client that connected successfully
     */
    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {
        try {
            sendSensorDescription(SensorType.Gyroscope, "Cursorgeschwindigkeit");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the user presses the reset position button
     *
     * @param origin which device pressed the button
     */
    @Override
    public void onResetPosition(NetworkDevice origin) {
        mMouseMover.resetPosToCenter();
    }
}
