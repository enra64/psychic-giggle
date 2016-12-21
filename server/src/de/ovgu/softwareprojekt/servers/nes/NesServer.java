package de.ovgu.softwareprojekt.servers.nes;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.filters.AverageMovementFilter;
import de.ovgu.softwareprojekt.filters.IntegratingFiler;
import de.ovgu.softwareprojekt.filters.ThresholdingFilter;
import de.ovgu.softwareprojekt.networking.Server;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * This server is designed to create NES controller input from gyroscope data
 */
public class NesServer extends Server {
    /**
     * Data we need to store for calculating the devices rotation
     */
    private float[] mValuesMagnet = new float[3];

    /**
     * Data we need to store for calculating the devices rotation
     */
    private float[] mValuesAccelerometer = new float[3];

    /**
     * Data we need to store for calculating the devices rotation
     */
    private float[] mValuesOrientation = new float[3];

    /**
     * Current device rotation matrix
     */
    private float[] mRotationMatrix = new float[9];

    /**
     *  The steering wheel emulates the peripheral device input
     */
    private SteeringWheel mSteeringWheel;

    private IntegratingFiler mIntegratingFilter;

    /**
     * preset list of controller button IDs
     */
    static final int A_BUTTON = 0, B_BUTTON = 1, X_BUTTON = 2, Y_BUTTON = 3, SELECT_BUTTON = 4, START_BUTTON = 5,
            R_BUTTON = 6, L_BUTTON = 7;

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName if not null, this name will be used. otherwise, the devices hostname is used
     */
    public NesServer(@Nullable String serverName) throws IOException, AWTException {
        super(serverName);

        mSteeringWheel = new SteeringWheel();

        //register use of gyroscope
        mIntegratingFilter = new IntegratingFiler(mSteeringWheel);
        NetworkDataSink gyroPipeline = mIntegratingFilter;
        setSensorOutputRange(SensorType.Gyroscope,100);
        registerDataSink(gyroPipeline, SensorType.Gyroscope);

        //register use of accelerometer
        //NetworkDataSink accPipeline = mSteeringWheel;  //LinearAcceleration ought to be filtered ... but what is the best approach?
        setSensorOutputRange(SensorType.LinearAcceleration,100);
        //registerDataSink(accPipeline, SensorType.LinearAcceleration);
        registerDataSink(new AverageMovementFilter(10, new AccelerationPhaseDetection(mSteeringWheel)), SensorType.LinearAcceleration);

    	setButtonLayout(readFile("../nesLayout.txt", "utf-8"));
    }

    /**
     * Called when an exception cannot be gracefully handled.
     *
     * @param origin    the instance (or, if it is a hidden instance, the known parent) that produced the exception
     * @param exception the exception that was thrown
     * @param info      additional information to help identify the problem
     */
    @Override
    public void onException(Object origin, Exception exception, String info) {
        System.out.println("onException:\n" + info);
        System.out.println("in " + origin.getClass());
        exception.printStackTrace();
        System.exit(0);
    }

    /**
     * Check whether a new client should be accepted
     *
     * @param newClient the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    @Override
    public boolean acceptClient(NetworkDevice newClient) {
        System.out.println("Player " + newClient.name + " connected");
        return true;
    }

    /**
     * Called when a client sent a disconnect signal
     *
     * @param disconnectedClient the lost client
     */
    @Override
    public void onClientDisconnected(NetworkDevice disconnectedClient) {
        System.out.println("Player " + disconnectedClient.name + " disconnected!");
    }

    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {
        System.out.println("Player " + timeoutClient.name + " had a timeout!");
    }

    @Override
    public void onResetPosition(NetworkDevice origin) {
        //Idea: Reset Integrating filter to 0 which means it acts as the initial position
        mIntegratingFilter.resetFilter();
    }

    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        mSteeringWheel.controllerInput(click.mID, true);
        if(!click.isHold)
            mSteeringWheel.controllerInput(click.mID, false);
    }

    static String readFile(String path, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
