package de.ovgu.softwareprojektapp.server_discovery;

import java.net.InetAddress;

/**
 * Encapsulates all information gathered about a server by the discovery engine
 */
public class Server {
    /**
     * Server name
     * TODO: give servers names
     */
    public String name;

    /**
     * The address of the server; directly usable with {@link InetAddress#getByName(String)}
     */
    public String address;

    /**
     * not implemented as of yet; value will be "unknown"
     * TODO: get port from discovery
     */
    public String port;

    public Server(InetAddress serverAddress) {
        name = serverAddress.toString();
        address = serverAddress.getHostAddress();
        port = "unknown";
    }
}
