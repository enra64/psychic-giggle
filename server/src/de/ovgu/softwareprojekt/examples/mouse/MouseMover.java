package de.ovgu.softwareprojekt.examples.mouse;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
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
    @SuppressWarnings("FieldCanBeLocal")
    private final int X_AXIS = 0, Z_AXIS = 2;

    /**
     * A class which is responsible for mouse behaviour
     *
     * @throws AWTException is thrown if low level input is not allowed on device
     */
    MouseMover() throws AWTException {
        mMoveBot = new Robot();
        resetPosToCenter();
    }

    /**
     * Reset mouse cursor to the center of the screen it currently is on
     */
    void resetPosToCenter() {
        //get mouse location
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // now get the configurations for each device
        for (GraphicsDevice device : graphicsEnvironment.getScreenDevices()) {
            for (GraphicsConfiguration config : device.getConfigurations()) {
                // check if the mouse is on this screen
                if (config.getBounds().contains(mouseLocation)) {
                    mMoveBot.mouseMove((int) config.getBounds().getCenterX(), (int) config.getBounds().getCenterY());
                    return;
                }
            }
        }

        // current screen not found, move to center of the default display
        GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
        mMoveBot.mouseMove(device.getDisplayMode().getWidth() / 2, device.getDisplayMode().getHeight() / 2);
    }

    /**
     * Press and release the right/left mouse button according to the button event
     *
     * @param buttonID  the button id as given in {@link de.ovgu.softwareprojekt.networking.Server#addButton(String, int)}
     * @param isClicked true if the user pressed, false if the user released the button
     */
    void click(int buttonID, boolean isClicked) {
        if (buttonID == MouseServer.LEFT_MOUSE_BUTTON) {
            if (isClicked)
                mMoveBot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            else
                mMoveBot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } else if (buttonID == MouseServer.RIGHT_MOUSE_BUTTON) {
            if (isClicked)
                mMoveBot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            else
                mMoveBot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        }
    }

    /**
     * moves Mouse according to rawData
     *
     * @param origin          the network device that sent this data
     * @param sensorData      the sensor data containing the x-,y-,z-Axis of gyroscope
     * @param userSensitivity the sensitivity the user chose for the sensorData.sensorType
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        Point mousePos = pi.getLocation();

        int xAxis = Math.round(sensorData.data[Z_AXIS]); //Up down movement on screen is achieved my rotating phone by z-axis
        int yAxis = Math.round(sensorData.data[X_AXIS]);

        mMoveBot.mouseMove(mousePos.x - xAxis, mousePos.y - yAxis);
    }

    /**
     * Needs to be implemented due to interface but serves no purpose here
     */
    @Override
    public void close() {
    }
}
