package de.ovgu.softwareprojekt.control.commands;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Signifies a connection request by a client device to a server.
 */
@SuppressWarnings("WeakerAccess")
public class ConnectionRequest extends Command {
    /**
     * Identify the device trying to connect. The net address is most probably not set!
     */
    public NetworkDevice self;

    /**
     * Create a new connection request
     * @param self identify the network device trying to connect. Must have at least command and data port set to be useful.
     */
    public ConnectionRequest(NetworkDevice self) {
        super(CommandType.ConnectionRequest);
        this.self = self;
    }
}
