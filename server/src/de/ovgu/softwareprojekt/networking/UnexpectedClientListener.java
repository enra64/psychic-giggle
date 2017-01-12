package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.net.InetAddress;

/**
 * Register for events where two clients try to use the same slot.
 * This happens when two clients get the same server description by the discovery system.
 */
public interface UnexpectedClientListener {
    /**
     * Called when a ClientConnection gets messages from an unknown client
     *
     * @param badClient address of the client sending to the wrong server
     * @param command the command that was received
     */
    void onUnexpectedClient(InetAddress badClient, AbstractCommand command);
}
