package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.Command;

import java.net.InetAddress;

/**
 * Subclass to be able to be notified of commands
 */
@SuppressWarnings("WeakerAccess")
public interface OnCommandListener {
    /**
     * Called whenever a new command arrives
     * @param origin hostname of whoever sent the packet
     * @param command the command that was sent
     */
    void onCommand(InetAddress origin, Command command);
}