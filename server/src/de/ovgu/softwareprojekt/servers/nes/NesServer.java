package de.ovgu.softwareprojekt.servers.nes;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder;
import de.ovgu.softwareprojekt.pipeline.filters.AveragingFilter;
import de.ovgu.softwareprojekt.networking.Server;
import de.ovgu.softwareprojekt.pipeline.filters.ThresholdingFilter;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This server is designed to create NES controller input from gyroscope data
 */
public class NesServer extends Server {
    /**
     *  The steering wheel emulates the peripheral device input
     */
    private SteeringWheel mSteeringWheel = new SteeringWheel();

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

        // normalize both utilized sensors
        setSensorOutputRange(SensorType.LinearAcceleration,10);
        setSensorOutputRange(SensorType.Gravity,100);

        // pipe the gravity sensor directly into the steering wheel
        registerDataSink(mSteeringWheel, SensorType.Gravity);

        // create and register the pipeline for the up/down detection
        registerAccelerationPhaseDetection();

        // display the nes button layout
        setButtonLayout(readFile("../nesLayout.txt", "utf-8"));
    }

    /**
     * Create a pipeline that filters linear acceleration data so that the acceleration phase
     * detection system can correctly recognize up/down events
     * @throws IOException if the data sink could not be registered
     */
    private void registerAccelerationPhaseDetection() throws IOException {
        // create the end, eg the phase detection system
        NetworkDataSink phaseDetection = new AccelerationPhaseDetection(mSteeringWheel);

        // create a filter pipeline ending in the acceleration phase detection system
        FilterPipelineBuilder pipelineBuilder = new FilterPipelineBuilder();
        pipelineBuilder.append(new ThresholdingFilter(null, .5f, 2));
        pipelineBuilder.append(new AveragingFilter(5));
        registerDataSink(pipelineBuilder.build(phaseDetection), SensorType.LinearAcceleration);
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
        // TODO: implement reset
    }

    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        mSteeringWheel.controllerInput(click.mID, click.isHold);
    }

    /**
     * Read all contents of a file into a string
     * @param path file location
     * @param encoding expected encoding
     * @return a string representing the file content
     * @throws IOException if the file could not be found or read
     */
    private static String readFile(String path, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
