package de.ovgu.softwareprojekt.servers.mouse;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Created by Ulrich on 11.11.2016.
 * <p>
 * This class gets the gyroscope data and moves the mouse accordingly
 */
public class MouseMover extends Mover {

    public MouseMover() {
        super();
        resetPosToCenter();
    }

    /**
     * Set Mouse Cursor Position to monitor position
     * //TODO:Test with more than one monitor
     * This method may break on devices which use more than one monitor?
     */
    public void resetPosToCenter() {
        //get mouse location
        Point point = MouseInfo.getPointerInfo().getLocation();

        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice[] devices = e.getScreenDevices();

        Rectangle displayBounds = null;

        //now get the configurations for each device
        for (int i=0;i<devices.length;i++) {

            GraphicsConfiguration[] configurations =
                    devices[i].getConfigurations();
            for (GraphicsConfiguration config: configurations) {
                Rectangle gcBounds = config.getBounds();

                if(gcBounds.contains(point)) {
                    displayBounds=gcBounds;
                    moveBot.mouseMove(devices[i].getDisplayMode().getWidth() / 2, devices[i].getDisplayMode().getHeight() / 2);
                }
            }
        }
        //not found, get the bounds for the default display
        if(displayBounds == null) {
            GraphicsDevice device = e.getDefaultScreenDevice();
            moveBot.mouseMove(device.getDisplayMode().getWidth() / 2, device.getDisplayMode().getHeight() / 2);

        }
    }

    /**
     * moves Mouse according to rawData
     *
     * @param data x-,y-,z-Axis of gyroscope
     */
    @Override
    public void move(float[] data) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        Point mousePos = pi.getLocation();

        int xAxis = (int) data[ZAXIS];   //Up down movement on screen is achieved my rotating phone by z-axis
        int yAxis = (int) data[XAXIS];

        moveBot.mouseMove(mousePos.x - xAxis, mousePos.y - yAxis);
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
