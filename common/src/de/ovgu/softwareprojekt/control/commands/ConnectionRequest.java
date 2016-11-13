package de.ovgu.softwareprojekt.control.commands;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

public class ConnectionRequest extends Command {
    /**
     * identify who is even trying to connect
     */
    public NetworkDevice self;

    public ConnectionRequest(NetworkDevice self) {
        super(CommandType.ConnectionRequest);
        this.self = self;
    }

}
