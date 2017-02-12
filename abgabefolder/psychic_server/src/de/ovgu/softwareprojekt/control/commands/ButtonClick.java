package de.ovgu.softwareprojekt.control.commands;

/**
 * Command class for signaling button clicks
 */
public class ButtonClick extends AbstractCommand {
    /**
     * Psychic ID of the button that was pressed
     */
    public int id;

    /**
     * True if the user pressed down; false if the user released the button
     */
    public boolean isPressed;

    /**
     * Create a new ButtonClick command
     *
     * @param id        psychic id of the button
     * @param isPressed true if the user pressed down; false if the user released the button
     */
    public ButtonClick(int id, boolean isPressed) {
        super(CommandType.ButtonClick);
        this.id = id;
        this.isPressed = isPressed;
    }

    /**
     * Get the id of the button as specified when creating it.
     *
     * @return psychic button id
     */
    public int getId() {
        return id;
    }

    /**
     * Get button press state
     *
     * @return true if the button was pressed down, false if it was released.
     */
    public boolean isPressed() {
        return isPressed;
    }
}
