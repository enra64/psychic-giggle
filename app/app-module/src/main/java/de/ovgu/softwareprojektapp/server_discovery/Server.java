package de.ovgu.softwareprojektapp.server_discovery;

import java.net.InetAddress;

import de.ovgu.softwareprojekt.discovery.ServerIdentification;

/**
 * Encapsulates all information gathered about a server by the discovery engine
 */
public class Server {
    /**
     * Server name
     */
    public String name;

    /**
     * The address of the server; directly usable with {@link InetAddress#getByName(String)}
     */
    public String address;

    /**
     * Contains the port the server announced
     */
    public String port;

    Server(ServerIdentification id, InetAddress addr) {
        port = id.port;
        name = id.name;
        address = addr.getHostAddress();
    }

    @Override
    public boolean equals(Object obj) {
        Server server = (Server) obj;
        return address.equals(server.address) && name.equals(server.name) && port.equals(server.port);
    }
}
