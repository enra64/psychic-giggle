package de.ovgu.softwareprojekt.servers.mouse;

import de.ovgu.softwareprojekt.Main;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Created by Ulrich on 11.11.2016.
 * <p>
 * This class gets the gyroscope data and moves the mouse accordingly
 */
public class MouseMover extends Mover {

    private float[] filteredData = new float[3];
    private final int averageSampleSize = 5;
    private float[][] average = new float[averageSampleSize][3];
    private int rawCount = 0; //used for average output

    private Point mousePos;
    //TODO: find the best sensitivity
    private final float SENSITIVITY = 40f;
    //TODO: user sensitivity feature in app
    //allow user to set own sensitivity
    private float customSensitivity = 0f;

    public MouseMover() {
        super();
        resetPosToCenter();
    }

    /**
     * Set Mouse Cursor Position to monitor position
     * This method may break on devices which use more than one monitor?
     */
    public void resetPosToCenter() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        moveBot.mouseMove(screenSize.width / 2, screenSize.height / 2);
    }

    /**
     * moves Mouse according to rawData
     *
     * @param rawData x-,y-,z-Axis of gyroscope
     */
    @Override
    public void move(float[] rawData) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        mousePos = pi.getLocation();

        float[] axesValues = filter(rawData);

        int xAxis = (int) axesValues[ZAXIS];   //Up down movement on screen is achieved my rotating phone by z-axis
        int yAxis = (int) axesValues[XAXIS];

        moveBot.mouseMove(mousePos.x - xAxis, mousePos.y - yAxis);
    }

    /**
     * takes rawData[] and turns it into useful data by applying the average of the last
     * last n gyroscope values. n = averageSampleSize
     *
     * @param rawData unfiltered gyroscope data
     * @return a float array which has useful axes values
     */

    //TODO: implement method
    public float[] filter(float[] rawData) {
        //TODO: filter value should be customizable
        rawData[XAXIS] *= (SENSITIVITY + customSensitivity);
        rawData[YAXIS] *= (SENSITIVITY + customSensitivity);
        rawData[ZAXIS] *= (SENSITIVITY + customSensitivity);

        //enter new gyroscope values
        average[rawCount][0] = rawData[XAXIS];
        average[rawCount][1] = rawData[YAXIS];
        average[rawCount][2] = rawData[ZAXIS];

        //create average of all values in the sample size
        for (int i = 0; i < averageSampleSize; i++) {
            filteredData[XAXIS] += average[rawCount][XAXIS];
            filteredData[YAXIS] += average[rawCount][YAXIS];
            filteredData[ZAXIS] += average[rawCount][ZAXIS];
        }
        rawCount++;

        //rawCount rotates through the array
        if (rawCount == averageSampleSize) {
            rawCount = 0;
        }

        //calculate average
        for (int i = 0; i < filteredData.length; i++) {
            filteredData[i] /= averageSampleSize;
        }

        return filteredData;
    }

    /**
     * This Method recieves a boolean and if true presses the left mouseButton and releases the button again if false
     *
     * @param isClicked
     */
    public void click(int buttonID, boolean isClicked) {
        if (buttonID == MouseServer.LEFT_MOUSE_BUTTON) {
            if (isClicked)
                moveBot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            else {
                moveBot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            }
        } else if (buttonID == MouseServer.RIGHT_MOUSE_BUTTON) {
            if (isClicked) {
                moveBot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            } else
                moveBot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        }

    }
}
