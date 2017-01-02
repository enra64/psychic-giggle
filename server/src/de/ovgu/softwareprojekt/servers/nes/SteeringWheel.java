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
 * Created by Ulrich on 12.12.2016.
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

    private int counter;

    /**
     * @throws AWTException is thrown when low level input is prohibit by system
     */
    public SteeringWheel() throws AWTException {
        mSteeringBot = new Robot(); //emulates peripheral device input
        lastAccActivation = System.currentTimeMillis();
        counter = 0;
    }

    public void controllerInput(int buttonID, boolean isPressed) {
        int event = -1;
        switch (buttonID) {
            case NesServer.A_BUTTON:
                event = KeyEvent.VK_X;
                break;
            case NesServer.B_BUTTON:
                // May use Z actually ingame ... but shouldn't
                event = KeyEvent.VK_Y;
                break;
            case NesServer.X_BUTTON:
                event = KeyEvent.VK_S;
                break;
            case NesServer.Y_BUTTON:
                event = KeyEvent.VK_A;
                break;
            case NesServer.SELECT_BUTTON:
                // Presses left shift or both shifts
                event = KeyEvent.VK_SHIFT;
                break;
            case NesServer.START_BUTTON:
                event = KeyEvent.VK_ENTER;
                break;
            case NesServer.R_BUTTON:
                event = KeyEvent.VK_C;
                break;
            case NesServer.L_BUTTON:
                event = KeyEvent.VK_D;
                break;
        }

        if (isPressed)
            mSteeringBot.keyPress(event);
        else
            mSteeringBot.keyRelease(event);
    }

    /**
     * onData is responsible for the controller input.
     * SensorData send here is either gyroscope or accelerometer
     * <br/>
     * The gyroscope steers the player and is responsible for left right movement in the menu
     * <br/>
     * the accelerometer is responsible for throwing items and up, down movement in the menu
     *
     * @param data            pipeline here is either the gyroscope or acc. pipeline //TODO das verstehe ich nicht -Arne
     * @param origin          where the sensor data comes from
     * @param userSensitivity the value set by the user. may be (as here) ignored when applying a custom sensitivity
     *                        is too annoying.
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        //Check if player steers to the left or right by tilted distance

        if(data.sensorType == SensorType.Gravity)
        {
            if (data.data[YAXIS] < -15)
                mSteeringBot.keyPress(KeyEvent.VK_LEFT);

            else if (data.data[YAXIS] > 15)
                mSteeringBot.keyPress(KeyEvent.VK_RIGHT);

            else {
                mSteeringBot.keyRelease(KeyEvent.VK_LEFT);
                mSteeringBot.keyRelease(KeyEvent.VK_RIGHT);
        }

        //TODO: Delete this shit if gravity works better
        if (data.sensorType == SensorType.Gyroscope) {
            onGyroscopeSteering(data);
        }
    }
    }

    private void onGyroscopeSteering(SensorData data){
        //TODO: DECIDE THRESHOLD VALUE FOR STEERING
        // info: when normalized to 100, the gyro on arne's device has a diff in integrated value of 230-260 for 90°
        if (data.data[ZAXIS] > 40)
            mSteeringBot.keyPress(KeyEvent.VK_LEFT);

        else if (data.data[ZAXIS] < -40)
            mSteeringBot.keyPress(KeyEvent.VK_RIGHT);

        else {
            mSteeringBot.keyRelease(KeyEvent.VK_LEFT);
            mSteeringBot.keyRelease(KeyEvent.VK_RIGHT);
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
        mSteeringBot.keyPress(KeyEvent.VK_DOWN);
        mSteeringBot.delay(5);
        mSteeringBot.keyPress(KeyEvent.VK_X);
        mSteeringBot.delay(30);
        mSteeringBot.keyRelease(KeyEvent.VK_X);
        mSteeringBot.keyRelease(KeyEvent.VK_DOWN);
    }

    @Override
    public void onDownMovement() {
        //WARNING: ONLY WORKS WHEN USING PROPERLY CONFIGURED COMPUTER
        try { Runtime.getRuntime().exec("notify-send --expire-time=50 down"); } catch (IOException ignored) { }

        System.out.println("onDown");
        mSteeringBot.keyPress(KeyEvent.VK_UP);
        mSteeringBot.delay(5);
        mSteeringBot.keyPress(KeyEvent.VK_X);
        mSteeringBot.delay(30);
        mSteeringBot.keyRelease(KeyEvent.VK_X);
        mSteeringBot.delay(5);
        mSteeringBot.keyRelease(KeyEvent.VK_UP);
    }
}
