package de.ovgu.softwareprojekt.discovery;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This object is used to identify our network devices
 */
@SuppressWarnings("WeakerAccess")
public class NetworkDevice implements Serializable {
    /**
     * A human-readable name
     */
    public String name;

    /**
     * port the device uses
     */
    public int port;


    /**
     * ip address
     */
    public String address;

    /**
     * Create a new, fully defined, NetworkDevice using the given parameters
     * @param name human readable name
     * @param port port the device uses
     * @param address ip address
     */
    public NetworkDevice(String name, int port, String address){
        this.name = name;
        this.port = port;
        this.address = address;
    }

    /**
     * Create a new, NetworkDevice when only the name is known
     * @param name human readable name
     */
    public NetworkDevice(String name){
        this(name, -1, "invalid");
    }

    /**
     * Returns this devices address to an {@link InetAddress} object
     * @return InetAddress pointing to this device
     * @throws UnknownHostException if this NetworkDevice is not configured sufficiently or the address could not be converted
     */
    public InetAddress getInetAddress() throws UnknownHostException {
        // check whether we actually have set a valid address yet
        if("invalid".equals(address))
            throw new UnknownHostException("This NetworkDevice is not configured sufficiently to provide an InetAddress");

        // convert and return address
        return InetAddress.getByName(address);
    }

    /**
     * Checks this device for total equivalence (eg name, port and address)
     * @return true if this device identifies itself as the obj and uses the same address and port
     */
    @Override
    public boolean equals(Object obj) {
        // if obj is not a NetworkDevice, they are not the same object
        if(!(obj instanceof NetworkDevice))
            return false;

        // for equality, all server parameters must be the same
        NetworkDevice server = (NetworkDevice) obj;
        return address.equals(server.address) && name.equals(server.name) && port == server.port;
    }
}
