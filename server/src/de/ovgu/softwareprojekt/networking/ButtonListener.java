package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.control.commands.ButtonClick;

/**
 * get notified when and which button is pressed
 */
public interface ButtonListener {


    void onButtonClick(ButtonClick click);
}
