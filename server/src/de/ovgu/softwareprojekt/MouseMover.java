package de.ovgu.softwareprojekt;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Created by Ulrich on 11.11.2016.
 *
 * This class gets the gyroscope data and moves the mouse accordingly
 */
public class MouseMover extends Mover {

    float averageX=0,averageY=0,averageZ=0;
    float[][] average = new float[5][3];
    int rawCount=0; //used for average output


    /** This is probably stupid, I just want to know what happens*/
    //TODO: Remove or improve this boolean bullshit dependent of this working or not
    private boolean isAlreadyClicked;


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
       isAlreadyClicked = false;
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



        //TODO: filter value should be customizable
        rawData[XAXIS] *= (SENSITIVITY +customSensitivity);
        rawData[YAXIS] *= (SENSITIVITY +customSensitivity);
        rawData[ZAXIS] *= (SENSITIVITY +customSensitivity);


        //TODO: average Data out of 5 inputs



        average[rawCount][0]=rawData[XAXIS];
        average[rawCount][1]=rawData[YAXIS];
        average[rawCount][2]=rawData[ZAXIS];

        for(int i=0;i<5;i++)
        {
            averageX = averageX + average[rawCount][0];
            averageY = averageY + average[rawCount][1];
            averageZ = averageZ + average[rawCount][2];
        }
        rawCount++;

        if(rawCount==5) {
            rawCount = 0;
        }

        averageX=averageX/5;
        averageY=averageY/5;
        averageZ=averageZ/5;

        rawData[XAXIS]=averageX;
        rawData[YAXIS]=averageY;
        rawData[ZAXIS]=averageZ;

        return rawData;


    }

    /**
     * This Method recieves a boolean and if true presses the left mouseButton and releases the button again if false
     * @param isClicked
     */
    public void click(boolean isClicked)
    {
        //TODO: Check later if this actually works as intended
        if(isClicked)
            if(!isAlreadyClicked) {
                moveBot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                isAlreadyClicked = true;
            }

        else {
                moveBot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                isAlreadyClicked = false;
        }
    }
}
