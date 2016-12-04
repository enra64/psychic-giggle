package de.ovgu.softwareprojekt.callback_interfaces;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Created by arne on 12/4/16.
 */
public interface ResetListener {
    void onResetPosition(NetworkDevice origin);
}
