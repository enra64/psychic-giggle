package de.ovgu.softwareprojekt.control.commands;

import java.util.List;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This command is used to check the connection, by requiring an answer to be sent if it is received
 */
public class ConnectionAliveCheck extends AbstractCommand {
    /**
     * The network device that is requesting the connection check
     */
    public NetworkDevice requester;

    /**
     * The network device that is answering the connection check
     */
    public NetworkDevice answerer = null;

    /**
     * Create a new connection check packet
     *
     * @param connectionCheckRequester the network device that is requesting the connection check
     */
    public ConnectionAliveCheck(NetworkDevice connectionCheckRequester) {
        super(CommandType.ConnectionAliveCheck);
        requester = connectionCheckRequester;
    }
}
