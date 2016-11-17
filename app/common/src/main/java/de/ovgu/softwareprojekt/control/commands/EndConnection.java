package de.ovgu.softwareprojekt.control.commands;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Signal an unilateral connection close
 */
public class EndConnection extends Command {
    /**
     * Identify the device trying to connect. The net address is most probably not set!
     */
    public NetworkDevice self;

    /**
     * Create a new connection request
     * @param self identify the network device trying to connect. Basically only needs the name, as
     *             the address will be appended by the receiver
     */
    public EndConnection(NetworkDevice self){
        super(CommandType.EndConnection);
        this.self = self;
    }
}
