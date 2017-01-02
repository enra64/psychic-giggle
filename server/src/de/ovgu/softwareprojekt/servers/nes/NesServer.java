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

    /**
     * This stack stores all available button configs. they will get taken out as more
     * clients connect, and stored back when clients are removed
     */
    private Stack<ButtonConfig> mButtonConfigs = new Stack<>();

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

        // load button mappings: which player presses what when
        loadButtonMappings();

    	// display the nes button layout
        super.setButtonLayout(readFile("../nesLayout.txt", "utf-8"));
    }

    /**
     * Loads all available button mappings from ../keys.properties
     * @throws IOException if the file could not be found
     */
    private void loadButtonMappings() throws IOException {
        FileInputStream input = new FileInputStream("../keys.properties");
        Properties prop = new Properties();

        // load a properties file
        prop.load(input);

        // get the property value and print it out
        for(int i = 3; i >= 0; i--){
            ButtonConfig newConfig = new ButtonConfig();
            newConfig.A = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_A_" + i).toCharArray()[0]);
            newConfig.B = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_B_" + i).toCharArray()[0]);
            newConfig.X = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_X_" + i).toCharArray()[0]);
            newConfig.Y = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_Y_" + i).toCharArray()[0]);
            newConfig.SELECT = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_SELECT_" + i).toCharArray()[0]);
            newConfig.START = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_START_" + i).toCharArray()[0]);
            newConfig.R = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_R_" + i).toCharArray()[0]);
            newConfig.L = ButtonConfig.getKeyEvent(prop.getProperty("BUTTON_L_" + i).toCharArray()[0]);
            mButtonConfigs.add(newConfig);
        }
    }

    /**
     * Create a pipeline that filters linear acceleration data so that the acceleration phase
     * detection system can correctly recognize up/down events
     * @throws IOException if the data sink could not be registered
     */
    private void registerAccelerationPhaseDetection(SteeringWheel wheel) throws IOException {
        // create the end, eg the phase detection system
        NetworkDataSink phaseDetection = new AccelerationPhaseDetection(wheel);

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
        try {
            // create a new steering wheel
            SteeringWheel newWheel = new SteeringWheel(mButtonConfigs.pop());

	// pipe the gravity sensor directly into the steering wheel
        registerDataSink(newWheel, SensorType.Gravity);

        // create and register the pipeline for the up/down detection
        registerAccelerationPhaseDetection(newWheel);

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

        // put back the button config
        mButtonConfigs.push(mSteeringWheels.get(disconnectedClient).getButtonConfig());

        mSteeringWheels.remove(disconnectedClient);
    }

    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {
        System.out.println("Player " + timeoutClient.name + " had a timeout!");

        // put back the button config that was used by the removed client
        mButtonConfigs.push(mSteeringWheels.get(timeoutClient).getButtonConfig());

        mSteeringWheels.remove(timeoutClient);
    }

    @Override
    public void onResetPosition(NetworkDevice origin) {
        // TODO: implement reset
    }

    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        mSteeringWheels.get(origin).controllerInput(click.mID, click.isHold);
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
