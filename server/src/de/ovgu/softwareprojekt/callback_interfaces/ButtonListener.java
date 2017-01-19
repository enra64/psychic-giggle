package de.ovgu.softwareprojekt.callback_interfaces;

import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * get notified when and which button is pressed
 */
public interface ButtonListener {
    /**
     * Called whenever a button is clicked
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    void onButtonClick(ButtonClick click, NetworkDevice origin);
}
