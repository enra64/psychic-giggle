package de.ovgu.softwareprojekt;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Created by Ulrich on 11.11.2016.
 *
 * This class gets the gyroscope data and moves the mouse accordingly
 */
public class MouseMover extends Mover {
    static int rawCount=0; //used for average output
    private Point mousePos;
    //TODO: find the best sensitivity
    private final float SENSITIVITY = 40f;
    //TODO: user sensitivity feature in app
    //allow user to set own sensitivity
    private float customSensitivity = 0f;
   public MouseMover()
   {
       super();
       resetPosToCenter();
   }

    /**
     * Set Mouse Cursor Position to monitor position
     * This method may break on devices which use more than one monitor?
     */
   public void resetPosToCenter(){
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       moveBot.mouseMove(screenSize.width/2, screenSize.height/2);
   }

    /**
     * moves Mouse according to rawData
     * @param rawData x-,y-,z-Axis of gyroscope
     */
    @Override
    public void move(float[] rawData) {
        mousePos = MouseInfo.getPointerInfo().getLocation();

        rawData = filter(rawData);

        int xAxis = (int) rawData[ZAXIS];   //Up down movement on screen is achieved my rotating phone by z-axis
        int yAxis = (int) rawData[XAXIS];

        //TODO: Find out if addition or subtraction works better
        moveBot.mouseMove(mousePos.x - xAxis, mousePos.y - yAxis);
    }

    /**
     *  takes rawData[] and turns it into useful data
     * @param rawData unfiltered gyroscope data
     * @return  a float array which has useful axes values
     */
    @Override
    //TODO: implement method
    public float[] filter(float[] rawData) {

        float[]nullarray={0,0,0};

        //TODO: filter value should be customizable
        rawData[XAXIS] *= (SENSITIVITY +customSensitivity);
        rawData[YAXIS] *= (SENSITIVITY +customSensitivity);
        rawData[ZAXIS] *= (SENSITIVITY +customSensitivity);


        //TODO: average Data out of 5 inputs

        float averageX=0,averageY=0,averageZ=0;
        float[][] average = new float[5][3];
        average[rawCount][1]=rawData[XAXIS];
        average[rawCount][2]=rawData[YAXIS];
        average[rawCount][3]=rawData[ZAXIS];

        rawCount++;
        if(rawCount==5) {
            rawCount = 0;
            for (int i = 1; i <= 5; i++) {
                averageX = averageX + average[1][i];
                averageY = averageY + average[2][i];
                averageZ = averageZ + average[3][i];
            }
            averageX=averageX/5;
            averageY=averageY/5;
            averageZ=averageZ/5;
            rawData[XAXIS]=averageX;
            rawData[YAXIS]=averageY;
            rawData[ZAXIS]=averageZ;

            return rawData;
        }
        return nullarray;
    }

    /**
     * This Method recieves a boolean and if true presses the left mouseButton and releases the button again if false
     * @param isClicked
     */
    public void click(boolean isClicked)
    {
        //TODO: Check later if this actually works as intended
        if(isClicked)
            moveBot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        else
            moveBot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
}
