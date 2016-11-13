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
     * discoveryPort the device is listening on this port for server discovery purposes
     */
    public int discoveryPort;

    /**
     * commandPort the device is listening on this port for incoming commands
     */
    public int commandPort;

    /**
     * dataPort the device is listening on this port for incoming data
     */
    public int dataPort;

    /**
     * ip address
     */
    public String address;

    /**
     * Create a new, fully defined, NetworkDevice using the given parameters
     * @param name human readable name
     * @param discoveryPort discoveryPort the device is listening on this port for server discovery purposes
     * @param commandPort the device is listening on this port for incoming commands
     * @param dataPort the device is listening on this port for incoming data
     * @param address ip address
     */
    public NetworkDevice(String name, int discoveryPort, int commandPort, int dataPort, String address){
        this.name = name;
        this.discoveryPort = discoveryPort;
        this.address = address;
    }

    /**
     * Create a new NetworkDevice which will announce itself as "name" who may be contacted using commandPort and dataPort
     * @param name human readable name
     * @param commandPort the device is listening on this port for incoming commands
     * @param dataPort the device is listening on this port for incoming data
     */
    public NetworkDevice(String name, int commandPort, int dataPort){
        this(name, -1, commandPort, dataPort, "invalid");
    }

    /**
     * Create a new NetworkDevice when only the name is known
     * @param name human readable name
     */
    public NetworkDevice(String name){
        this(name, -1, -1, -1, "invalid");
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
     * Checks this device for total equivalence (eg name, discoveryPort and address)
     * @return true if this device identifies itself as the obj and uses the same address and discoveryPort
     */
    @Override
    public boolean equals(Object obj) {
        // if obj is not a NetworkDevice, they are not the same object
        if(!(obj instanceof NetworkDevice))
            return false;

        // for equality, only the name and address must be the same. thus, a device can have multiple servers,
        // and a name may be used multiple times in the same network
        NetworkDevice server = (NetworkDevice) obj;
        return address.equals(server.address) && name.equals(server.name);
    }
}
