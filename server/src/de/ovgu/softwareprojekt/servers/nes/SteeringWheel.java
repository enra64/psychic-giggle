package de.ovgu.softwareprojekt.servers.nes;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by Ulrich on 12.12.2016.
 * This class is responsible for steering the player in Mario Kart
 * TODO: better description for this class
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
     * @throws AWTException is thrown when low level input is prohibit by system
     */
    public SteeringWheel() throws AWTException {
        mSteeringBot = new Robot();
    }

    public void controllerInput(int buttonID, boolean isPressed)
    {
        //TODO: PROVIDE control.cfg for emulator or decide for a better solution

        //The following robot input is set by the button layout of a standard SNES controller
        switch(buttonID){
            case NesServer.A_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_A);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_A);
                break;
            case NesServer.B_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_B);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_B);
                break;
            case NesServer.X_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_X);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_X);
                break;
            case NesServer.Y_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_Y);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_Y);
                break;
            case NesServer.SELECT_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_SPACE);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_SPACE);
                break;
            case NesServer.START_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_ENTER);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_ENTER);
                break;
            case NesServer.R_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_R);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_R);
                break;
            case NesServer.L_BUTTON:
                if(isPressed)
                    mSteeringBot.keyPress(KeyEvent.VK_L);
                else
                    mSteeringBot.keyRelease(KeyEvent.VK_L);
                break;
            default:
                break;
        }
    }

    /**
     * onData is responsible for the controller input.
     * the SensorData here is a filtered additive value of the ZAXIS for steering the kart.
     * Steering in Super Mario Kart is based on binary values 1 - steers in direction
     *      and 0 - does not steer in this direction
     * @param data
     */
    @Override
    public void onData(SensorData data) {

        if(data.sensorType == SensorType.Gyroscope) {
            //TODO: DECIDE THRESHOLD VALUE FOR STEERING
            if (data.data[ZAXIS] < -5)
                mSteeringBot.keyPress(KeyEvent.VK_LEFT);

            else if (data.data[ZAXIS] > 5)
                mSteeringBot.keyPress(KeyEvent.VK_RIGHT);

            else{
                mSteeringBot.keyRelease(KeyEvent.VK_LEFT);
                mSteeringBot.keyRelease(KeyEvent.VK_RIGHT);
            }
        }

        //TODO: Decide how to deal with this Accelerometer
        if(data.sensorType == SensorType.Accelerometer){
        }
    }

    @Override
    public void close() {

    }
}
