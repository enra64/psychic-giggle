package de.ovgu.softwareprojekt.servers.nes;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Created by Ulrich on 12.12.2016
 * This class is responsible for steering the player in simple racing games
 */
public class SteeringWheel implements NetworkDataSink, AccelerationPhaseDetection.AccelerationListener {

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
     * This button config stores the awt buttons that should be pressed on certain app button
     * presses
     */
    private ButtonConfig mButtonConfig;

    /**
     * @throws AWTException is thrown when low level input is prohibit by system
     */
    SteeringWheel(ButtonConfig config, NetworkDevice newClient) throws AWTException {
        mSteeringBot = new Robot(); //emulates peripheral device input
        lastAccActivation = System.currentTimeMillis();
        mButtonConfig = config;
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
     * @param data            contains sensor data
     * @param origin          where the sensor data comes from
     * @param userSensitivity the value set by the user. may be (as here) ignored when applying a custom sensitivity
     *                        is too annoying.
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        //Check if player steers to the left or right by tilted distance

        if(data.sensorType == SensorType.Gravity)
        {
            if (data.data[YAXIS] < -userSensitivity)
                mSteeringBot.keyPress(mButtonConfig.LEFT);
            else if (data.data[YAXIS] > userSensitivity)
                mSteeringBot.keyPress(mButtonConfig.RIGHT);

            else {
                mSteeringBot.keyRelease(mButtonConfig.LEFT);
                mSteeringBot.keyRelease(mButtonConfig.RIGHT);
            }
        }
    }


    /**
     * Needs to be implemented due to interface but serves no purpose here
     */
    @Override
    public void close() {
    }

    @Override
    public void onUpMovement() {
        //WARNING: ONLY WORKS WHEN USING PROPERLY CONFIGURED COMPUTER
        try { Runtime.getRuntime().exec("notify-send --expire-time=50 up"); } catch (IOException ignored) { }

        System.out.println("onUp");
        mSteeringBot.keyPress(mButtonConfig.DOWN);
        mSteeringBot.delay(5);
        mSteeringBot.keyPress(mButtonConfig.A);
        mSteeringBot.delay(30);
        mSteeringBot.keyRelease(mButtonConfig.A);
        mSteeringBot.keyRelease(mButtonConfig.DOWN);
    }

    @Override
    public void onDownMovement() {
        //WARNING: ONLY WORKS WHEN USING PROPERLY CONFIGURED COMPUTER (i3, notify-send)
        try { Runtime.getRuntime().exec("notify-send --expire-time=50 down"); } catch (IOException ignored) { }

        System.out.println("onDown");
        mSteeringBot.keyPress(mButtonConfig.UP);
        mSteeringBot.delay(5);
        mSteeringBot.keyPress(mButtonConfig.A);
        mSteeringBot.delay(30);
        mSteeringBot.keyRelease(mButtonConfig.A);
        mSteeringBot.delay(5);
        mSteeringBot.keyRelease(mButtonConfig.UP);
    }
}
