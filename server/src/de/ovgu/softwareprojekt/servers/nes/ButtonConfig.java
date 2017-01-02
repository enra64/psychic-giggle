package de.ovgu.softwareprojekt.servers.nes;

import org.omg.CORBA.DynAnyPackage.Invalid;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

/**
 * Created by arne on 12/19/16.
 */
public class ButtonConfig {
    public int A, B, X, Y, SELECT, START, R, L;

    public ButtonConfig(){

    }

    public ButtonConfig(int a, int b, int x, int y, int SELECT, int START, int r, int l) {
        A = a;
        B = b;
        X = x;
        Y = y;
        this.SELECT = SELECT;
        this.START = START;
        R = r;
        L = l;
    }

    /**
     * Map a button id to a awt key event code
     * @param psychicButtonId the button code sent by the app
     * @return null, or the corresponding awt button code
     */
    public Integer mapInput(int psychicButtonId) {
        switch (psychicButtonId) {
            case NesServer.A_BUTTON:
                return A;
            case NesServer.B_BUTTON:
                // May use Z actually ingame ... but shouldn't
                return B;
            case NesServer.X_BUTTON:
                return X;
            case NesServer.Y_BUTTON:
                return Y;
            case NesServer.SELECT_BUTTON:
                // Presses left shift or both shifts
                return SELECT;
            case NesServer.START_BUTTON:
                return START;
            case NesServer.R_BUTTON:
                return R;
            case NesServer.L_BUTTON:
                return L;
            default:
                return null;
        }
    }
    
    static int getKeyEvent(String keyEvent) throws InvalidButtonException {
        try {
            Field keyEventType = KeyEvent.class.getField(keyEvent);
            return keyEventType.getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new InvalidButtonException("\"" + keyEvent + "\"" + " could not be recognized.");
        }
    }
}
