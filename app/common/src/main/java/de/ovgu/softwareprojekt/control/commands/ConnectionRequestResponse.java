package de.ovgu.softwareprojekt.control.commands;

/**
 * This command may be used to switch sensors on or off
 */
public class ConnectionRequestResponse extends AbstractCommand {
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
