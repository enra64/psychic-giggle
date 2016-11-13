package de.ovgu.softwareprojekt;

import java.awt.*;

/**
 * Created by Ulrich on 11.11.2016.
 *
 * This abstact class defines the Robotclass that controls various applications by smartphone movement
 */
public abstract class Mover{

    protected Robot moveBot;

    public Mover(){

        try {
            moveBot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    //Maybe change parameters later?
    public abstract void move(float[] rawData);

    public abstract float[] filter(float[] rawData);

}
