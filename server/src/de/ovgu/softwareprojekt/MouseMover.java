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
     * @param rawData x,y,zAxis of gyroscope
     */
    @Override
    public void move(float[] rawData) {
        mousePos = MouseInfo.getPointerInfo().getLocation();

        int yAxis = (int) rawData[0];
        int xAxis = (int) rawData[2];
        //TODO:
        //add or subtract gyroscop values
        moveBot.mouseMove(mousePos.x + xAxis, mousePos.y + yAxis);
    }

    /**
     *  takes rawData[] and turns it into useful data
     * @param rawData unfiltered gyroscope data
     * @return  an array that can be turned into integer values
     */
    @Override
    public float[] filter(float[] rawData) {
        return new float[0];
    }
}
