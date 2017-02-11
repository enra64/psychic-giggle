package de.ovgu.softwareprojekt.callback_interfaces;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This callback interface is used for processing the "Reset Position" button in the app
 */
public interface ResetListener {
    /**
     * Called when the user presses the reset position button
     *
     * @param origin which device pressed the button
     */
    void onResetPosition(NetworkDevice origin);
}
