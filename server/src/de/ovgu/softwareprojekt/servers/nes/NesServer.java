package de.ovgu.softwareprojekt.servers.nes;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.filters.IntegratingFiler;
import de.ovgu.softwareprojekt.filters.ThresholdingFilter;
import de.ovgu.softwareprojekt.networking.Server;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * This server is designed to create NES controller input from gyroscope data
 */
public class NesServer extends Server {
    /**
     * This variable stores our steering wheels, mapped from network devices so we
     * can easily access the correct one.
     */
    private HashMap<NetworkDevice, SteeringWheel> mSteeringWheels = new HashMap<>();

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

        loadButtonMappings();

    	super.setButtonLayout(readFile("../nesLayout.txt", "utf-8"));
    }

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

            //register use of gyroscope
            IntegratingFiler integrator = new IntegratingFiler(newWheel);
            newWheel.setIntegratingFilter(integrator);
            registerDataSink(integrator, SensorType.Gyroscope);
            setSensorOutputRange(SensorType.Gyroscope,100);

            //register use of accelerometer
            setSensorOutputRange(SensorType.LinearAcceleration,100);
            registerDataSink(newWheel, SensorType.LinearAcceleration);

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
        //Idea: Reset Integrating filter to 0 which means it acts as the initial position
        mSteeringWheels.get(origin).resetIntegratingFilter();
    }

    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        mSteeringWheels.get(origin).controllerInput(click.mID, true);
        if(!click.isHold)
            mSteeringWheels.get(origin).controllerInput(click.mID, false);
    }

    private static String readFile(String path, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
