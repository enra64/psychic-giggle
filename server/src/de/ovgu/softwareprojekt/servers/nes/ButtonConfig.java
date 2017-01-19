package de.ovgu.softwareprojekt.servers.nes;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

/**
 * POJO for mapping psychic button ids to java robot keycodes
 */
class ButtonConfig implements Comparable<ButtonConfig> {
    /**
     * This maps from psychic button ids (keys) to java button ids (values).
     */
    private HashMap<Integer, Integer> mMapping = new HashMap<>();

    /**
     * This value represents the identification number for each player
     */
    private int playerID;

    /**
     * Create a new ButtonConfig.
     *
     * @param buttonProperties the properties object that is the source for all button configs
     * @param player           the player index for which the config should be read
     * @throws InvalidButtonException if a button could not be parsed
     */
    ButtonConfig(Properties buttonProperties, int player) throws InvalidButtonException {
        playerID = player;
        addButton(buttonProperties, PsychicNesButton.A_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.B_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.X_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.Y_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.SELECT_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.START_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.R_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.L_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.LEFT_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.UP_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.RIGHT_BUTTON, player);
        addButton(buttonProperties, PsychicNesButton.DOWN_BUTTON, player);
    }

    /**
     * Add a mapping for a button to mMapping
     *
     * @param buttonProperties the properties object storing the button mapping
     * @param button           the button that should be added
     * @param player           which player the button is for
     * @throws InvalidButtonException if a button could not be parsed
     */
    private void addButton(Properties buttonProperties, PsychicNesButton button, int player) throws InvalidButtonException {
        // get psychic button id and java button id
        int psychicButtonId = button.psychicId();
        int javaButtonId = getKeyEvent(buttonProperties.getProperty(button.name() + "_" + player));

        // add to mapping
        mMapping.put(psychicButtonId, javaButtonId);
    }

    /**
     * Convert a VK_<KEY> string to a KeyEvent.VK_<KEY> integer
     *
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
     * Get the ID used for the player device this button mapping is assigned to
     *
     * @return the ID used for the player device this button mapping is assigned to
     */
    int getPlayerID() {
        return playerID;
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

    /**
     * Compare ButtonConfigs by the player they represent
     *
     * @param other the ButtonConfig that should be compared to
     * @return the value 0 if this == other; a value less than 0 if this < other;
     * and a value greater than 0 if this > other
     */
    @Override
    public int compareTo(ButtonConfig other) {
        return Integer.compare(playerID, other.getPlayerID());
    }
}
