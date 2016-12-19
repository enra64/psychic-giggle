package de.ovgu.softwareprojekt.servers.nes;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.filters.IntegratingFiler;
import sun.security.krb5.internal.ktab.KeyTabConstants;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by Ulrich on 12.12.2016.
 * This class is responsible for steering the player in simple racing games
 */
public class SteeringWheel implements NetworkDataSink {

    /**
     * is responsible for emulating hardware input
     */
    private Robot mSteeringBot;

    /**
     * Constants which describe which index of sensorData.data is responsible for which axis
     */
    private final int XAXIS = 0, YAXIS = 1, ZAXIS = 2;

    /**
     * Meassures the time intervall between two accelerationSensor usages
     */
    private long lastAccActivation;

    /**
     * This filter is used for integrating the gyroscope data
     */
    private IntegratingFiler mIntegratingFilter;

    /**
     * This button config stores the awt buttons that should be pressed on certain app button
     * presses
     */
    private ButtonConfig mButtonConfig;

    private int counter;

    /**
     * @throws AWTException is thrown when low level input is prohibit by system
     */
    SteeringWheel(ButtonConfig config) throws AWTException {
        mSteeringBot = new Robot(); //emulates peripheral device input
        lastAccActivation = System.currentTimeMillis();
        counter = 0;
        mButtonConfig = config;
    }

    /**
     * Set the integrating filter used for this steering wheel
     * @param filter the filter to be used
     */
    void setIntegratingFilter(IntegratingFiler filter) {
        mIntegratingFilter = filter;
    }

    /**
     * Call {@link IntegratingFiler#resetFilter()}.
     */
    void resetIntegratingFilter() {
        mIntegratingFilter.resetFilter();
    }

    /**
     * Map app button input to robot events
     * @param buttonID the pressed buttons id
     * @param isPressed whether it is currently pressed or not
     */
    void controllerInput(int buttonID, boolean isPressed) {
        Integer event = mButtonConfig.mapInput(buttonID);

        if (event != null) {
            if (isPressed)
                mSteeringBot.keyPress(event);
            else
                mSteeringBot.keyRelease(event);
        }
    }

    /**
     * Get the button config this steering wheel uses
     */
    ButtonConfig getButtonConfig(){
        return mButtonConfig;
    }

    /**
     * onData is responsible for the controller input.
     * SensorData send here is either gyroscope or accelerometer
     * <br/>
     * The gyroscope steers the player and is responsible for left right movement in the menu
     * <br/>
     * the accelerometer is responsible for throwing items and up, down movement in the menu
     *
     * @param data pipeline here is either the gyroscope or acc. pipeline
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data) {
        //Check if player steers to the left or right by tilted distance
        if (data.sensorType == SensorType.Gyroscope) {
            //TODO: DECIDE THRESHOLD VALUE FOR STEERING
            if (data.data[ZAXIS] > 2500)
                mSteeringBot.keyPress(KeyEvent.VK_LEFT);

            else if (data.data[ZAXIS] < -2500)
                mSteeringBot.keyPress(KeyEvent.VK_RIGHT);
            else {
                mSteeringBot.keyRelease(KeyEvent.VK_LEFT);
                mSteeringBot.keyRelease(KeyEvent.VK_RIGHT);
            }
        }

        //TODO: Decide how to deal with this Accelerometer correctly; random magic numbers place holder
        //Decide if item should be thrown forwards or backwards
        if (data.sensorType == SensorType.LinearAcceleration) {
            if (data.data[ZAXIS] > 500) {
                if (checkInterval()) {
                    mSteeringBot.keyPress(KeyEvent.VK_UP);
                    mSteeringBot.keyPress(KeyEvent.VK_A);

                } else {
                    mSteeringBot.keyRelease(KeyEvent.VK_A);
                    mSteeringBot.keyRelease(KeyEvent.VK_UP);
                }
            }

            if (data.data[ZAXIS] < -500) {
                if (checkInterval()) {
                    mSteeringBot.keyPress(KeyEvent.VK_DOWN);
                    mSteeringBot.keyPress(KeyEvent.VK_A);

                } else {
                    mSteeringBot.keyRelease(KeyEvent.VK_A);
                    mSteeringBot.keyRelease(KeyEvent.VK_DOWN);
                }
            }
        }
    }

    /**
     * This method checks if the last valid acceleration happened 500ms ago
     *
     * @return
     */
    private boolean checkInterval() {
        long newActivation = System.currentTimeMillis();
        //Check if last activation happened 500 or more milliseconds ago
        if (newActivation - lastAccActivation > 500) {
            lastAccActivation = newActivation;
            return true;
        } else
            return false;
    }

    /**
     * Needs to be implemented due to interface but serves no purpose here
     */
    @Override
    public void close() {
    }
}
