package de.ovgu.softwareprojekt.control.commands;

/**
 * This command is sent as an answer to {@link ConnectionRequest} commands, to notify of the decision
 * to accept the client (or not).
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
