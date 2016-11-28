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
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        DisplayMode dm = gs[0].getDisplayMode();
        moveBot.mouseMove(dm.getWidth() / 2, dm.getHeight() / 2);
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
