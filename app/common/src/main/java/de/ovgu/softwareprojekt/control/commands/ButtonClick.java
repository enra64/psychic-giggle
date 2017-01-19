package de.ovgu.softwareprojekt.control.commands;

/**
 * Command class for signalling button clicks
 */
public class ButtonClick extends AbstractCommand {
    /**
     * Psychic ID of the button that was pressed
     */
    public int mID;

    /**
     * True if the user pressed down; false if the user released the button
     */
    public boolean isHold;

    /**
     * Create a new ButtonClick command
     * @param id psychic id of the button
     * @param isHold true if the user pressed down; false if the user released the button
     */
    public ButtonClick(int id, boolean isHold){
        super(CommandType.ButtonClick);
        mID = id;
        this.isHold = isHold;
    }
}
