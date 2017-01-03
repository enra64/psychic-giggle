package de.ovgu.softwareprojekt.servers.nes;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

/**
 * Created by arne on 12/19/16.
 */
class ButtonConfig {
    int A, B, X, Y, SELECT, START, R, L, RIGHT, LEFT, UP, DOWN;

    /**
     * preset list of controller button IDs
     */
    private static final int A_BUTTON = 0, B_BUTTON = 1, X_BUTTON = 2, Y_BUTTON = 3, SELECT_BUTTON = 4, START_BUTTON = 5,
            R_BUTTON = 6, L_BUTTON = 7, LEFT_BUTTON = 8, UP_BUTTON = 9, RIGHT_BUTTON = 10, DOWN_BUTTON = 11;

    ButtonConfig(){
    }

    /**
     * Map a button id to a awt key event code
     *
     * @param psychicButtonId the button code sent by the app
     * @return null, or the corresponding awt button code
     */
    public Integer mapInput(int psychicButtonId) {
        switch (psychicButtonId) {
            case A_BUTTON:
                return A;
            case B_BUTTON:
                return B;
            case X_BUTTON:
                return X;
            case Y_BUTTON:
                return Y;
            // Presses left shift or both shifts
            case SELECT_BUTTON:
                return SELECT;
            case START_BUTTON:
                return START;
            case R_BUTTON:
                return R;
            case L_BUTTON:
                return L;
            case LEFT_BUTTON:
                return LEFT;
            case UP_BUTTON:
                return UP;
            case RIGHT_BUTTON:
                return RIGHT;
            case DOWN_BUTTON:
                return DOWN;
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
