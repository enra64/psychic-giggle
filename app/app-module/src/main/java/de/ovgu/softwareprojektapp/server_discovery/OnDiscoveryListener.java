package de.ovgu.softwareprojektapp.server_discovery;

import java.util.List;

/**
 * Interface you want to implement to be notified of new discovery results
 */
public interface OnDiscoveryListener {

    /**
     * Called whenever the server list is changed
     * @param servers list of all current servers
     */
    void onServerListUpdated(List<Server> servers);
}
