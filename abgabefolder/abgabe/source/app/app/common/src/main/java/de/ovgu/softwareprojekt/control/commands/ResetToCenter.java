package de.ovgu.softwareprojekt.control.commands;

/**
 * This command is to be used for resetting the phone position, whatever
 * that means for an application. (A mouse server would put the mouse in
 * the screen center, for example)
 */
public class ResetToCenter extends AbstractCommand {

    /**
     * This command is to be used for resetting the phone position, whatever
     * that means for an application. (A mouse server would put the mouse in
     * the screen center, for example)
     */
    public ResetToCenter() {
        super(CommandType.ResetToCenter);
    }
}
