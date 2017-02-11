package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.AbstractCommand;

import java.net.InetAddress;

/**
 * Implement this to be able to be notified of commands
 */
@SuppressWarnings("WeakerAccess")
public interface OnCommandListener {
    /**
     * Called whenever a new command arrives
     * @param origin hostname of whoever sent the packet
     * @param command the command that was sent
     */
    void onCommand(InetAddress origin, AbstractCommand command);
}