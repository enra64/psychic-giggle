package de.ovgu.softwareprojekt.servers.nes;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.Server;
import de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder;
import de.ovgu.softwareprojekt.pipeline.filters.AveragingFilter;
import de.ovgu.softwareprojekt.pipeline.filters.ThresholdingFilter;
import de.ovgu.softwareprojekt.pipeline.filters.UserSensitivityMultiplicator;
import de.ovgu.softwareprojekt.pipeline.splitters.ClientSplitter;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;

/**
 * This server is designed to create NES controller input from gyroscope data
 */
public class NesServer extends Server {
    /**
     * This variable stores our steering wheels, mapped from network devices so we
     * can easily access the correct one.
     */
    private HashMap<NetworkDevice, SteeringWheel> mSteeringWheels = new HashMap<>();

    private HashMap<NetworkDevice, NetworkDataSink> mAccPhaseDetectors = new HashMap<>();

    /**
     * This stack stores all available button configs. they will get taken out as more
     * clients connect, and stored back when clients are removed
     */
    private Stack<ButtonConfig> mButtonConfigs = new Stack<>();

    /**
     * This client splitter makes sure that all linear acceleration data is only sent to the correct pipeline
     */
    private ClientSplitter mLinearAccelerationSplitter = new ClientSplitter();

    /**
     * This client splitter makes sure that all gravity data is only sent to the correct pipeline
     */
    private ClientSplitter mGravitySplitter = new ClientSplitter();

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName if not null, this name will be used. otherwise, the devices hostname is used
     */
    public NesServer(@Nullable String serverName) throws IOException, AWTException {
        super(serverName);

        // normalize both utilized sensors
        setSensorOutputRange(SensorType.LinearAcceleration, 10);
        setSensorOutputRange(SensorType.Gravity, 333);

        // load button mappings: which player presses what when
        loadButtonMappings();

        // display the nes button layout
        super.setButtonLayout(readFile("../nesLayout.txt", "utf-8"));

        registerDataSink(mGravitySplitter, SensorType.Gravity);
        registerDataSink(mLinearAccelerationSplitter, SensorType.LinearAcceleration);
    }

    /**
     * Loads all available button mappings from ../keys.properties
     *
     * @throws IOException if the file could not be found
     */
    private void loadButtonMappings() throws IOException {
        FileInputStream input = new FileInputStream("../keys.properties");
        Properties prop = new Properties();

        // load a properties file
        prop.load(input);

        // get the property value and print it out
        for (int player = 3; player >= 0; player--) {
            try {
                ButtonConfig newConfig = new ButtonConfig();
                newConfig.A = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_A_" + player));
                newConfig.B = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_B_" + player));
                newConfig.X = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_X_" + player));
                newConfig.Y = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_Y_" + player));
                newConfig.SELECT = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_SELECT_" + player));
                newConfig.START = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_START_" + player));
                newConfig.R = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_R_" + player));
                newConfig.L = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_L_" + player));
                newConfig.LEFT = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_LEFT_" + player));
                newConfig.UP = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_UP_" + player));
                newConfig.RIGHT = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_RIGHT_" + player));
                newConfig.DOWN = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_DOWN_" + player));
                mButtonConfigs.add(newConfig);
            } catch (InvalidButtonException e) {
                System.out.println("Player " + player + ": Keycode " + e.getMessage());
            }
        }

    }

    /**
     * Create a pipeline that filters linear acceleration data so that the acceleration phase
     * detection system can correctly recognize up/down events
     *
     * @param wheel the SteeringWheel getting the up/down events
     * @throws IOException if the data sink could not be registered
     */
    private NetworkDataSink getAccelerationPhaseDetection(SteeringWheel wheel) throws IOException {
        // create the end, eg the phase detection system
        NetworkDataSink phaseDetection = new AccelerationPhaseDetection(wheel);

        // create a filter pipeline ending in the acceleration phase detection system
        FilterPipelineBuilder pipelineBuilder = new FilterPipelineBuilder();
        pipelineBuilder.append(new ThresholdingFilter(null, .5f, 2));
        pipelineBuilder.append(new AveragingFilter(5));
        return pipelineBuilder.build(phaseDetection);
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
        try {
            // create a new steering wheel
            SteeringWheel newWheel = new SteeringWheel(mButtonConfigs.pop(), newClient);

            // pipe the gravity sensor directly into the steering wheel
            mGravitySplitter.addDataSink(newClient, newWheel);

            // create, store and register the pipeline for the up/down detection
            NetworkDataSink apd = getAccelerationPhaseDetection(newWheel);
            mLinearAccelerationSplitter.addDataSink(newClient, apd);
            mAccPhaseDetectors.put(newClient, apd);

            // add the steering wheel to the list of network devices
            mSteeringWheels.put(newClient, newWheel);
        } catch (AWTException | IOException e) {
            e.printStackTrace();
            // yeah this shouldnt happen
        }

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

        removeClient(disconnectedClient);
    }

    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {
        System.out.println("Player " + timeoutClient.name + " had a timeout!");

        removeClient(timeoutClient);
    }

    /**
     * De-initializes a client in this server
     * @param removeClient the client to be removed
     */
    private void removeClient(NetworkDevice removeClient){
        if(mSteeringWheels.get(removeClient) != null){
            // put back the button config that was used by the removed client
            mButtonConfigs.push(mSteeringWheels.get(removeClient).getButtonConfig());

            // delete the steering wheel
            mSteeringWheels.remove(removeClient);
        }

        // unregister its data sinks
        try {
            unregisterDataSink(mAccPhaseDetectors.get(removeClient));
            unregisterDataSink(mSteeringWheels.get(removeClient));
        } catch (IOException | NullPointerException ignored) {
        }
    }

    @Override
    public void onResetPosition(NetworkDevice origin) {
    }

    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        mSteeringWheels.get(origin).controllerInput(click.mID, click.isHold);
    }

    /**
     * Read all contents of a file into a string
     *
     * @param path     file location
     * @param encoding expected encoding
     * @return a string representing the file content
     * @throws IOException if the file could not be found or read
     */
    @SuppressWarnings("SameParameterValue")
    private static String readFile(String path, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
