package de.ovgu.softwareprojekt.servers.nes;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

/**
 * POJO for mapping psychic button ids to java robot keycodes
 */
class ButtonConfig {
    /**
     * Psychic button ids
     */
    private static final int A_BUTTON = 0, B_BUTTON = 1, X_BUTTON = 2, Y_BUTTON = 3, SELECT_BUTTON = 4, START_BUTTON = 5,
            R_BUTTON = 6, L_BUTTON = 7, LEFT_BUTTON = 8, UP_BUTTON = 9, RIGHT_BUTTON = 10, DOWN_BUTTON = 11;

    /**
     * This maps from psychic button ids (keys) to java button ids (values).
     */
    private HashMap<Integer, Integer> mMapping = new HashMap<>();

    /**
     * Create a new ButtonConfig.
     *
     * @param buttonProperties the properties object that is the source for all button configs
     * @param player           the player index for which the config should be read
     * @throws InvalidButtonException if a button could not be parsed
     */
    ButtonConfig(Properties buttonProperties, int player) throws InvalidButtonException {
        mMapping.put(A_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_A_" + player)));
        mMapping.put(B_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_B_" + player)));
        mMapping.put(X_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_X_" + player)));
        mMapping.put(Y_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_Y_" + player)));
        mMapping.put(SELECT_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_SELECT_" + player)));
        mMapping.put(START_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_START_" + player)));
        mMapping.put(R_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_R_" + player)));
        mMapping.put(L_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_L_" + player)));
        mMapping.put(LEFT_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_LEFT_" + player)));
        mMapping.put(UP_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_UP_" + player)));
        mMapping.put(RIGHT_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_RIGHT_" + player)));
        mMapping.put(DOWN_BUTTON, getKeyEvent(buttonProperties.getProperty("BUTTON_DOWN_" + player)));
    }

    /**
     * Get a collection of all java keys this ButtonConfig maps to
     */
    Collection<Integer> getJavaButtons() {
        return mMapping.values();
    }

    /**
     * Map a button id to a awt key event code
     *
     * @param psychicButtonId the button code sent by the app
     * @return null, or the corresponding awt button code
     */
    Integer mapInput(int psychicButtonId) {
        return mMapping.get(psychicButtonId);
    }

    /**
     * Convert a VK_<KEY> string to a KeyEvent.VK_<KEY> integer
     * @param keyEvent a string representation of the key id
     * @return an integer representation of the key id
     * @throws InvalidButtonException if the string could not be converted into a KeyEvent integer
     */
    private static int getKeyEvent(String keyEvent) throws InvalidButtonException {
        try {
            Field keyEventType = KeyEvent.class.getField(keyEvent);
            return keyEventType.getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new InvalidButtonException("\"" + keyEvent + "\"" + " could not be recognized.");
        }
    }
}
