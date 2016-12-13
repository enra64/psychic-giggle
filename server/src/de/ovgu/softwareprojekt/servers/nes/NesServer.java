package de.ovgu.softwareprojekt.servers.nes;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.Server;

import java.io.IOException;
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
     * preset list of controller button IDs
     */
    static final int A_BUTTON = 0, B_BUTTON = 1, X_BUTTON = 2, Y_BUTTON = 3, SELECT_BUTTON = 4, START_BUTTON = 5,
            R_BUTTON = 6, L_BUTTON = 7;

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName if not null, this name will be used. otherwise, the devices hostname is used
     */
    public NesServer(@Nullable String serverName) throws IOException {
        super(serverName);

        //TODO: Do we really use the RotationVector here or shouldn't it be gyroscope+accelerometer
        registerDataSink(new DataSink() {
            @Override
            public void onData(SensorData sensorData) {
                //TODO: do whatever you do
            }

            @Override
            public void close() {

            }
        }, SensorType.RotationVector);
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
    }

    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
    }
}
