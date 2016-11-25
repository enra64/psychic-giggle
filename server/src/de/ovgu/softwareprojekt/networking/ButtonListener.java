package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.control.commands.ButtonClick;

/**
 * get notified when and which button is pressed
 */
public interface ButtonListener {
    /**
     * Called whenever a button is clicked
     *
     * @param click event object specifying details like button id
     */
    void onButtonClick(ButtonClick click);
}
