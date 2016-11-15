package de.ovgu.softwareprojekt;

import java.awt.*;

/**
 * Created by Ulrich on 11.11.2016.
 *
 * This class gets the gyroscope data and moves the mouse accordingly
 */
public class MouseMover extends Mover {

    private Point mousePos;

   public MouseMover()
   {
       super();
   }

    /**
     * moves Mouse according to rawData
     * @param rawData x-,y-,z-Axis of gyroscope
     */
    @Override
    public void move(float[] rawData) {
        mousePos = MouseInfo.getPointerInfo().getLocation();

        int yAxis = (int) rawData[ZAXIS];   //Up down movement on screen is achieved my moving phone by z-axis
        int xAxis = (int) rawData[XAXIS];

        //TODO: Find out if addition or subtraction works better
        moveBot.mouseMove(mousePos.x + xAxis, mousePos.y + yAxis);
    }

    /**
     *  takes rawData[] and turns it into useful data
     * @param rawData unfiltered gyroscope data
     * @return  a float array which has useful axes values
     */
    @Override
    //TODO: implement method
    public float[] filter(float[] rawData) {
        return rawData;
    }
}
