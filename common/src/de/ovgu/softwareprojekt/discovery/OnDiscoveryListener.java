package de.ovgu.softwareprojekt.discovery;

import java.util.List;

import de.ovgu.softwareprojekt.discovery.DeviceIdentification;

/**
 * Interface you want to implement to be notified of new discovery results
 */
public interface OnDiscoveryListener {

    /**
     * Called whenever the server list is changed
     * @param servers list of all current servers
     */
    void onServerListUpdated(List<DeviceIdentification> servers);
}
