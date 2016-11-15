package de.ovgu.softwareprojekt.control.commands;

import de.ovgu.softwareprojekt.SensorType;

/**
 * This command may be used to switch sensors on or off
 */
public class ConnectionRequestResponse extends Command {
    /**
     * Whether the request was granted or not
     */
    public boolean grant;

    /**
     * Create a new response for a connection request
     * @param grant Whether the connection request was granted or not
     */
    public ConnectionRequestResponse(boolean grant) {
        super(CommandType.ConnectionRequestResponse);
        this.grant = grant;
    }
}
