package de.ovgu.softwareprojekt.servers.nes;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.AbstractServer;
import de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder;
import de.ovgu.softwareprojekt.pipeline.filters.AveragingFilter;
import de.ovgu.softwareprojekt.pipeline.filters.NormalizationFilter;
import de.ovgu.softwareprojekt.pipeline.splitters.ClientSplitter;
import de.ovgu.softwareprojekt.util.FileUtils;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Properties;

/**
 * This server is designed to create NES controller input from gravity and linear acceleration data
 */
public class NesServer extends AbstractServer {
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
     * This client splitter makes sure that linear acceleration data is only sent to the correct pipeline
     */
    private ClientSplitter mLinearAccelerationSplitter = new ClientSplitter();

    /**
     * This client splitter makes sure that gravity data is only sent to the correct pipeline
     */
    private ClientSplitter mGravitySplitter = new ClientSplitter();

    /**
     * This value represents an id number which is used by the Android NotificationManager to identify notifications
     */
    private final int PLAYER_NUM_NOTIFICATION_ID = 0;

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName if not null, this name will be used. otherwise, the devices hostname is used
     * @throws IOException  if an issue with the phone communication arose
     * @throws AWTException if your device does not support javas {@link Robot} input
     */
    public NesServer(@Nullable String serverName) throws IOException, AWTException {
        super(serverName);

        // normalize both utilized sensors
        NetworkDataSink gravityPipeline = new NormalizationFilter(333f, 10f, mGravitySplitter);

        // load button mappings: which player presses what when
        loadButtonMappings();

        // display the nes button layout on all connected devices
        super.setButtonLayout(FileUtils.readFile("../nesLayout.xml", "utf-8"));

        // register our data splitters for sensor data
        registerDataSink(gravityPipeline, SensorType.Gravity);
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
     * @return a NetworkDataSink representing the beginning of the {@link AccelerationPhaseDetection} pipeline
     * @throws IOException if the data sink could not be registered
     */
    private NetworkDataSink getAccelerationPhaseDetection(SteeringWheel wheel) throws IOException {
        // create the end, eg the phase detection system
        NetworkDataSink phaseDetection = new AccelerationPhaseDetection(wheel);

        // create a filter pipeline ending in the acceleration phase detection system
        FilterPipelineBuilder pipelineBuilder = new FilterPipelineBuilder();
        pipelineBuilder.append(new NormalizationFilter(1000f, 40f,new AveragingFilter(5)));
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
        System.out.println("NesServer experienced exception in " + origin.getClass() + ", info: " + info);
        System.out.flush();
        exception.printStackTrace();
        System.exit(1);
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
        } catch (AWTException e) {
            System.out.println("Your device does not support java virtual input, which is required.");
            return false;
        } catch (IOException e) {
            onException(this, e, "Could not accept client");
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
            wheel.close();

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

        // close all steering wheels
        mSteeringWheels.forEach((networkDevice, wheel) -> wheel.close());
        mAccPhaseDetectors.forEach((networkDevice, apd) -> apd.close());
    }

    /**
     * Reset position is not implemented
     *
     * @param origin which device pressed the button
     */
    @Override
    public void onResetPosition(NetworkDevice origin) {
    }

    /**
     * Forward button clicks to the steering wheel handling the device that sent the click
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        mSteeringWheels.get(origin).controllerInput(click.getId(), click.isPressed());
    }

    /**
     * Called when a client has finished connecting
     *
     * @param connectedClient the client that connected successfully
     */
    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {
        // Get playerID and add 1, because IDs start with zero
        int playerID = mSteeringWheels.get(connectedClient).getButtonConfig().getPlayerID() + 1;

        try {
            displayNotification(PLAYER_NUM_NOTIFICATION_ID, "Player " + playerID, "You are connected as player " + playerID, connectedClient.getInetAddress());

            sendSensorDescription(SensorType.LinearAcceleration, "Itemwurfempfindlichkeit");
            sendSensorDescription(SensorType.Gravity, "Lenkverhalten");


        } catch (IOException e) {
            onException(this, e, "Could not display notification for player " + playerID);
        }


    }
}
