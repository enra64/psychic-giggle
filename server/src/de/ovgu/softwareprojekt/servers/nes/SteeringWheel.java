package de.ovgu.softwareprojekt.servers.nes;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by Ulrich on 12.12.2016.
 * This class is responsible for steering the player in Mario Kart
 * TODO: better description for this class
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
     * @throws AWTException is thrown when low level input is prohibit by system
     */
    public SteeringWheel() throws AWTException {
        mSteeringBot = new Robot();
    }

    public void controllerInput(int buttonID, boolean isPressed)
    {
        //TODO: PROVIDE control.cfg for emulator or decide for a better solution
        //I thought it is actually smarter to set it to the emulator default input - Ulrich

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
    public void onData(NetworkDevice origin, SensorData data) {
        if(data.sensorType == SensorType.Gyroscope) {
            //TODO: DECIDE THRESHOLD VALUE FOR STEERING
            if (data.data[ZAXIS] > 20000)
                mSteeringBot.keyPress(KeyEvent.VK_LEFT);

            else if (data.data[ZAXIS] < -30000)
                mSteeringBot.keyPress(KeyEvent.VK_RIGHT);
            else{
                mSteeringBot.keyRelease(KeyEvent.VK_LEFT);
                mSteeringBot.keyRelease(KeyEvent.VK_RIGHT);
            }
        }

        //TODO: Decide how to deal with this Accelerometer correctly; 20 is a random magic number place holder
//        if(data.sensorType == SensorType.Accelerometer){
//            if(data.data[ZAXIS] > 3500) {
//                mSteeringBot.keyPress(KeyEvent.VK_UP);
//                mSteeringBot.keyPress(KeyEvent.VK_X);
//                mSteeringBot.keyRelease(KeyEvent.VK_X);
//                mSteeringBot.keyRelease(KeyEvent.VK_UP);
//            }
//
//            if(data.data[ZAXIS] < -350){
//                mSteeringBot.keyPress(KeyEvent.VK_DOWN);
//                mSteeringBot.keyPress(KeyEvent.VK_X);
//                mSteeringBot.keyRelease(KeyEvent.VK_X);
//                mSteeringBot.keyRelease(KeyEvent.VK_DOWN);
//            }
    }
    // }

    /**
     * Needs to be implemented due to interface but serves no purpose here
     */
    @Override
    public void close() {
    }
}
