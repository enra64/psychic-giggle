package de.ovgu.softwareprojekt.discovery;

import java.util.List;

/**
 * This interface is used by the Discovery system to notify of changes to the server list
 */
public interface OnDiscoveryListener {
    /**
     * Called when the list of current devices changed
     *
     * @param deviceList a list of current {@link NetworkDevice NetworkDevices}
     */
    void onServerListUpdated(List<NetworkDevice> deviceList);
}
