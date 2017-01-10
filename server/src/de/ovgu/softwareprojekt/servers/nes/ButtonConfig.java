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
        mMapping.put(PsychicNesButton.A_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_A_" + player)));
        mMapping.put(PsychicNesButton.B_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_B_" + player)));
        mMapping.put(PsychicNesButton.X_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_X_" + player)));
        mMapping.put(PsychicNesButton.Y_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_Y_" + player)));
        mMapping.put(PsychicNesButton.SELECT_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_SELECT_" + player)));
        mMapping.put(PsychicNesButton.START_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_START_" + player)));
        mMapping.put(PsychicNesButton.R_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_R_" + player)));
        mMapping.put(PsychicNesButton.L_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_L_" + player)));
        mMapping.put(PsychicNesButton.LEFT_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_LEFT_" + player)));
        mMapping.put(PsychicNesButton.UP_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_UP_" + player)));
        mMapping.put(PsychicNesButton.RIGHT_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_RIGHT_" + player)));
        mMapping.put(PsychicNesButton.DOWN_BUTTON.psychicId(), getKeyEvent(buttonProperties.getProperty("BUTTON_DOWN_" + player)));
    }

    /**
     * Convert a VK_<KEY> string to a KeyEvent.VK_<KEY> integer
     * @param keyEvent a string representation of the key psychicButtonId
     * @return an integer representation of the key psychicButtonId
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

    /**
     * Get a collection of all java keys this ButtonConfig maps to
     */
    Collection<Integer> getJavaButtons() {
        return mMapping.values();
    }

    /**
     * Map a psychicNesButton to a awt key event code
     *
     * @param psychicNesButton the button code sent by the app
     * @return null, or the corresponding awt button code
     */
    Integer mapInput(PsychicNesButton psychicNesButton) {
        return mMapping.get(psychicNesButton.psychicId());
    }

    /**
     * Map a button psychicButtonId to a awt key event code
     *
     * @param psychicButtonId the button code sent by the app
     * @return null, or the corresponding awt button code
     */
    Integer mapInput(Integer psychicButtonId) {
        return mMapping.get(psychicButtonId);
    }
}
