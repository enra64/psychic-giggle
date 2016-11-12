package de.ovgu.softwareprojekt.discovery;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Identify a server when answering to a discovery request
 */
@SuppressWarnings("WeakerAccess")
public class DeviceIdentification implements Serializable {
    public String name;
    public int port;
    public String address;

    public DeviceIdentification(String name, int port, String address){
        this.name = name;
        this.port = port;
        this.address = address;
    }

    public DeviceIdentification(String name){
        this(name, -1, "");
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByName(address);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DeviceIdentification){
            DeviceIdentification server = (DeviceIdentification) obj;

            // for equality, all server parameters must be the same
            return address.equals(server.address) && name.equals(server.name) && port == server.port;
        }

        // if obj is not a DeviceIdentification, they are not the same object
        return false;
    }
}
