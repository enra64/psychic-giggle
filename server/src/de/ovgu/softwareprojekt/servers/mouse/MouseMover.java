package de.ovgu.softwareprojekt.servers.mouse;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Created by Ulrich on 11.11.2016.
 * <p>
 * This class gets the gyroscope data and moves the mouse accordingly
 */
public class MouseMover implements DataSink {

    /**
     * is responsible for emulating hardware input
     */
    private Robot mMoveBot;

    /**
     * Constants which describe which index of sensorData.data is responsible for which axis
     */
    private final int XAXIS = 0, YAXIS = 1, ZAXIS = 2;

    /**
     * A class which is responsible for mouse behaviour
     * @throws AWTException is thrown if low level input is not allowed on device
     */
    public MouseMover() throws AWTException {

        mMoveBot = new Robot();

        resetPosToCenter();
    }

    /**
     * Set Mouse Cursor Position to monitor position
     */

    //TODO:Test with more than one monitor, may glitch on multimonitors
    public void resetPosToCenter() {
        //get mouse location (which screen)
        Point point = MouseInfo.getPointerInfo().getLocation(); //might be wrong, has to be tested!!!

        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice[] devices = e.getScreenDevices();

        //now get the configurations for each device
        for (int i=0;i<devices.length;i++) {

            GraphicsConfiguration[] configurations = devices[i].getConfigurations();
            for (GraphicsConfiguration config: configurations) {
                Rectangle gcBounds = config.getBounds();

                if(gcBounds.contains(point)) {
                    mMoveBot.mouseMove((int) config.getBounds().getCenterX(), (int) config.getBounds().getCenterY());
                    return;
                }
            }
        }
        //not found, get the bounds for the default display
        GraphicsDevice device = e.getDefaultScreenDevice();
        mMoveBot.mouseMove(device.getDisplayMode().getWidth() / 2, device.getDisplayMode().getHeight() / 2);
    }

    /**
     * moves Mouse according to rawData
     *
     * @param data x-,y-,z-Axis of gyroscope
     */
    public void move(float[] data) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        Point mousePos = pi.getLocation();

        int xAxis = (int) data[ZAXIS];   //Up down movement on screen is achieved my rotating phone by z-axis
        int yAxis = (int) data[XAXIS];

        mMoveBot.mouseMove(mousePos.x - xAxis, mousePos.y - yAxis);
    }

    /**
     * This Method receives a boolean and if true presses the left mouseButton and releases the button again if false
     *
     * @param isClicked
     */
    public void click(int buttonID, boolean isClicked) {
        if (buttonID == MouseServer.LEFT_MOUSE_BUTTON) {
            if (isClicked)
                mMoveBot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            else {
                mMoveBot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            }
        } else if (buttonID == MouseServer.RIGHT_MOUSE_BUTTON) {
            if (isClicked) {
                mMoveBot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            } else
                mMoveBot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        }

    }

    @Override
    public void onData(SensorData sensorData) {
        move(sensorData.data);
    }

    @Override
    public void close() {

    }
}
