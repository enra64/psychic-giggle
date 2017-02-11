package de.ovgu.softwareprojekt.callback_interfaces;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Implement to be notified of client events:
 * <p>
 * <ul>
 * <li>{@link #acceptClient(NetworkDevice)} for checking whether you want to accept a new client</li>
 * <li>{@link #onClientAccepted(NetworkDevice)} called when a client has successfully connected</li>
 * <li>{@link #onClientDisconnected(NetworkDevice)} when a client leaves, or lost connection</li>
 * <li>{@link #onClientTimeout(NetworkDevice)} when a client no longer responds</li>
 * </ul>
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
     * Called when a client sent a disconnect signal or has disconnected
     *
     * @param disconnectedClient the lost client
     */
    void onClientDisconnected(NetworkDevice disconnectedClient);

    /**
     * Called when a client hasn't responded in a while
     *
     * @param timeoutClient the client that did not respond
     */
    void onClientTimeout(NetworkDevice timeoutClient);

    /**
     * Called when a Client has successfully been connected
     *
     * @param connectedClient the client that connected successfully
     */
    void onClientAccepted(NetworkDevice connectedClient);
}
