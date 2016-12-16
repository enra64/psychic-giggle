package de.ovgu.softwareprojekt.servers.nes;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by Ulrich on 12.12.2016.
 * This class is responsible for steering the player in simple racing games
 */
public class SteeringWheel implements DataSink{

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
     * @throws AWTException is thrown when low level input is prohibit by system
     */
    public SteeringWheel() throws AWTException {
        mSteeringBot = new Robot(); //emulates peripheral device input
        lastAccActivation = System.currentTimeMillis();
    }

    public void controllerInput(int buttonID, boolean isPressed)
    {
        //The following robot input is set by the button layout of a standard SNES controller
        switch(buttonID){
            case NesServer.A_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_X);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_X);
                break;
            case NesServer.B_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_Y); //May use Z actually ingame ... but shouldn't
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_Y);
                break;
            case NesServer.X_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_S);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_S);
                break;
            case NesServer.Y_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_A);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_A);
                break;
            case NesServer.SELECT_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_SHIFT); //Presses left shift or both shifts
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_SHIFT);
                break;
            case NesServer.START_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_ENTER);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_ENTER);
                break;
            case NesServer.R_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_C);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_C);
                break;
            case NesServer.L_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_D);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_D);
                break;
            default:
                break;
        }
    }

    /**
     * onData is responsible for the controller input.
     * SensorData send here is either gyroscope or accelerometer
     * <br/>
     * The gyroscope steers the player and is responsible for left right movement in the menu
     * <br/>
     * the accelerometer is responsible for throwing items and up, down movement in the menu
     * @param data pipeline here is either the gyroscope or acc. pipeline
     */
    @Override
    public void onData(SensorData data) {
        //Check if player steers to the left or right by tilted distance
        if(data.sensorType == SensorType.Gyroscope) {
            //TODO: DECIDE THRESHOLD VALUE FOR STEERING
            if (data.data[ZAXIS] > 40000)
                mSteeringBot.keyPress(KeyEvent.VK_LEFT);

            else if (data.data[ZAXIS] < -40000)
                mSteeringBot.keyPress(KeyEvent.VK_RIGHT);
            else{
                mSteeringBot.keyRelease(KeyEvent.VK_LEFT);
                mSteeringBot.keyRelease(KeyEvent.VK_RIGHT);
            }
        }

        //TODO: Decide how to deal with this Accelerometer correctly; random magic numbers place holder
        //Decide if item should be thrown forwards or backwards
        if(data.sensorType == SensorType.LinearAcceleration){
            if(data.data[ZAXIS] > 500) {
                if(checkInterval()) {
                    mSteeringBot.keyPress(KeyEvent.VK_UP);
                    mSteeringBot.keyPress(KeyEvent.VK_X);
                    mSteeringBot.keyRelease(KeyEvent.VK_X);
                    mSteeringBot.keyRelease(KeyEvent.VK_UP);
                }
            }

            if(data.data[ZAXIS] < -500){
                if(checkInterval()) {
                    mSteeringBot.keyPress(KeyEvent.VK_DOWN);
                    mSteeringBot.keyPress(KeyEvent.VK_X);
                    mSteeringBot.keyRelease(KeyEvent.VK_X);
                    mSteeringBot.keyRelease(KeyEvent.VK_DOWN);
                }
            }
    }
}

    /**
     * This method checks if the last valid acceleration happened 500ms ago
     * @return
     */
    private boolean checkInterval(){
        long newActivation = System.currentTimeMillis();
        //Check if last activation happened 500 or more milliseconds ago
        if(newActivation - lastAccActivation > 500){
            lastAccActivation = newActivation;
            return true;
        }
        else
            return false;
    }

    /**
     * Needs to be implemented due to interface but serves no purpose here
     */
    @Override
    public void close() {
    }
}
