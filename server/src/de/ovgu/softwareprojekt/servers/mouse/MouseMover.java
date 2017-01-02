package de.ovgu.softwareprojekt.servers.mouse;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Created by Ulrich on 11.11.2016.
 * <p>
 * This class gets the gyroscope data and moves the mouse accordingly
 */
public class MouseMover implements NetworkDataSink {

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

    public void resetPosToCenter() {
        //get mouse location (which screen)
        Point point = MouseInfo.getPointerInfo().getLocation();

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

    /**
     * moves Mouse according to rawData
     *
     * @param sensorData the sensor data containing the x-,y-,z-Axis of gyroscope
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        Point mousePos = pi.getLocation();

        int xAxis = (int) sensorData.data[ZAXIS];   //Up down movement on screen is achieved my rotating phone by z-axis
        int yAxis = (int) sensorData.data[XAXIS];

        mMoveBot.mouseMove(mousePos.x - xAxis, mousePos.y - yAxis);
    }

    /**
     * Needs to be implemented due to interface but serves no purpose here
     */
    @Override
    public void close() {
    }
}
