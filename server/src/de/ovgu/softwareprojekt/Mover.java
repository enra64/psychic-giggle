package de.ovgu.softwareprojekt;

import java.awt.*;

/**
 * Created by Ulrich on 11.11.2016.
 *
 * This abstact class defines the Robotclass that controls various applications by smartphone movement
 */
public abstract class Mover{

    //methods work with float arrays which contain various sensor data of directional axes
    protected final int XAXIS = 0;
    protected final int YAXIS = 1;
    protected final int ZAXIS = 2;

    //moveBot is responsible for emulating device inputs
    protected Robot moveBot;

    public Mover(){

        try {
            moveBot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


    public abstract void move(float[] rawData);

    public abstract float[] filter(float[] rawData);
}
