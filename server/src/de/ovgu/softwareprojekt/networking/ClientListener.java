package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Implement to be notified of new or disconnected clients
 */
public interface ClientListener {
    /**
     * Check whether a new client should be accepted
     *
     * @param newClient the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    boolean acceptClient(NetworkDevice newClient);

    /**
     * Called when a client sent a disconnect signal
     *
     * @param disconnectedClient the lost client
     */
    void onClientDisconnected(NetworkDevice disconnectedClient);

    /**
     * Called when a client hasn't responded to connection check requests within 500ms
     *
     * @param timeoutClient the client that did not respond
     */
    void onClientTimeout(NetworkDevice timeoutClient);
}
