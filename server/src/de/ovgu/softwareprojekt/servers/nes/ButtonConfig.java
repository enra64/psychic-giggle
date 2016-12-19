package de.ovgu.softwareprojekt.servers.nes;

import java.awt.*;
import java.awt.event.KeyEvent;

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
    
    static int getKeyEvent(char character){
        switch (character) {
            case 'a': return KeyEvent.VK_A;
            case 'b': return KeyEvent.VK_B;
            case 'c': return KeyEvent.VK_C;
            case 'd': return KeyEvent.VK_D;
            case 'e': return KeyEvent.VK_E;
            case 'f': return KeyEvent.VK_F;
            case 'g': return KeyEvent.VK_G;
            case 'h': return KeyEvent.VK_H;
            case 'i': return KeyEvent.VK_I;
            case 'j': return KeyEvent.VK_J;
            case 'k': return KeyEvent.VK_K;
            case 'l': return KeyEvent.VK_L;
            case 'm': return KeyEvent.VK_M;
            case 'n': return KeyEvent.VK_N;
            case 'o': return KeyEvent.VK_O;
            case 'p': return KeyEvent.VK_P;
            case 'q': return KeyEvent.VK_Q;
            case 'r': return KeyEvent.VK_R;
            case 's': return KeyEvent.VK_S;
            case 't': return KeyEvent.VK_T;
            case 'u': return KeyEvent.VK_U;
            case 'v': return KeyEvent.VK_V;
            case 'w': return KeyEvent.VK_W;
            case 'x': return KeyEvent.VK_X;
            case 'y': return KeyEvent.VK_Y;
            case 'z': return KeyEvent.VK_Z;
            default:
                throw new IllegalArgumentException("Cannot type character " + character);
        }
    }
}
