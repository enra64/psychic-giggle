package de.ovgu.softwareprojekt.control.commands;


/**
 * Command class for hiding the reset button in SendActivity
 */
public class HideReset extends AbstractCommand {

    /**
     * This boolean decides if the reset button should be shown
     */
    private boolean isHidden = false;

    public HideReset(boolean isHidden){
        super(CommandType.HideReset);
        this.isHidden = isHidden;
    }

    /**
     * Shows if the Reset Button is visible.
     * @return
     */
    public boolean isHidden() {
        return isHidden;
    }
}
