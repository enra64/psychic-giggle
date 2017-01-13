package de.ovgu.softwareprojekt.servers.nes;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.ClientConnection;
import de.ovgu.softwareprojekt.networking.Server;
import de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder;
import de.ovgu.softwareprojekt.pipeline.filters.AveragingFilter;
import de.ovgu.softwareprojekt.pipeline.splitters.ClientSplitter;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Stack;

/**
 * This server is designed to create NES controller input from gravity and linear acceleration data
 */
public class NesServer extends Server {
    /**
     * This variable stores our steering wheels, mapped from network devices so we
     * can easily access the correct one.
     */
    private HashMap<NetworkDevice, SteeringWheel> mSteeringWheels = new HashMap<>();

    /**
     * This variable stores our acceleration phase detectors, mapped from network devices
     * so we can easily access the one responsible for any given network device
     */
    private HashMap<NetworkDevice, NetworkDataSink> mAccPhaseDetectors = new HashMap<>();

    /**
     * This priority queue stores all available button configs. they will get taken out as more
     * clients connect, and stored back when clients are removed.
     * Queue is automatically sorted by playerID stored in ButtonConfig
     */
    private PriorityQueue<ButtonConfig> mButtonConfigs = new PriorityQueue<>();
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
        setSensorOutputRange(SensorType.LinearAcceleration, 1000);
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
        for (int player = 0; player < 4; player++) {
            try {
                mButtonConfigs.add(new ButtonConfig(prop, player));
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
            SteeringWheel wheel = new SteeringWheel(mButtonConfigs.poll());

            // create, store and register the pipeline for the up/down detection
            NetworkDataSink apd = getAccelerationPhaseDetection(wheel);

            // add the new client as a target for data
            mGravitySplitter.addDataSink(newClient, wheel);
            mLinearAccelerationSplitter.addDataSink(newClient, apd);

            // store a mapping from this network device to the new control stuff
            mSteeringWheels.put(newClient, wheel);
            mAccPhaseDetectors.put(newClient, apd);

            // add the steering wheel to the list of network devices
            mSteeringWheels.put(newClient, wheel);
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
     *
     * @param removeClient the client to be removed
     */
    private void removeClient(NetworkDevice removeClient) {
        SteeringWheel wheel = mSteeringWheels.get(removeClient);        
        if (wheel != null) {
            // put back the button config that was used by the removed client
            mButtonConfigs.add(wheel.getButtonConfig());
            wheel.releaseAllKeys();

            // delete the steering wheel
            mSteeringWheels.remove(removeClient);

            // remove the client from the splitter
            mLinearAccelerationSplitter.remove(removeClient);
            mGravitySplitter.remove(removeClient);
        }

        // unregister its data sinks
        try {
            unregisterDataSink(mAccPhaseDetectors.get(removeClient), SensorType.LinearAcceleration);
            unregisterDataSink(mSteeringWheels.get(removeClient), SensorType.Gravity);
        } catch (IOException | NullPointerException ignored) {
        }
    }

    @Override
    public void close() {
        super.close();

        for (SteeringWheel sw : mSteeringWheels.values())
            sw.releaseAllKeys();
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

    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {
        //Get playerID and add 1, because IDs start with zero
        int playerID = mSteeringWheels.get(connectedClient).getButtonConfig().getPlayerID()+1;

        try {
            displayNotification(0, "Player " + playerID, "You are connected as player " + playerID, true, connectedClient.getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
