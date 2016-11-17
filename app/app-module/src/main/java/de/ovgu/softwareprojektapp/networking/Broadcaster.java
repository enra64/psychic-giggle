package de.ovgu.softwareprojektapp.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import de.ovgu.softwareprojekt.discovery.DiscoveryThread;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

/**
 * This class can be used together with {@link java.util.Timer} to periodically send broadcasts
 * into the network.
 */
class Broadcaster extends TimerTask {
    /**
     * The address used for broadcasting to the network.
     * The default is simply 255.255.255.255, but because broadcasting from a hotspot is a very
     * interesting use case, we have a whole lotta code dedicated to detecting being a hotspot
     */
    private final List<InetAddress> mBroadcastAddresses;

    /**
     * BREAK GLASS IN CASE OF EXCEPTION
     */
    private final ExceptionListener mExceptionListener;

    /**
     * The port the discovery server must be listening on
     */
    private int mRemotePort;

    /**
     * The discovery thread used to actually broadcast the information
     */
    private DiscoveryThread mDiscoveryThread;

    /**
     * Create a new broadcaster.
     *
     * @param discoveryThread required for {@link DiscoveryThread#sendSelfIdentification(InetAddress, int)}
     * @param remotePort      required for sending broadcasts
     * @throws IOException when we cannot detect the correct broadcast address
     */
    Broadcaster(DiscoveryThread discoveryThread, ExceptionListener exceptionListener, int remotePort) throws IOException {
        mRemotePort = remotePort;
        mDiscoveryThread = discoveryThread;
        mExceptionListener = exceptionListener;


        mBroadcastAddresses = getBroadcastAddresses();
    }

    /**
     * Return a list of all useful broadcast addresses of this device
     */
    private List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> resultList = new LinkedList<>();

        try {
            // iterate over all network interfaces
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // iterate over the interfaces addresses
                for (InterfaceAddress ifAddress : networkInterface.getInterfaceAddresses()) {
                    // avoid loopback interfaces
                    if (!ifAddress.getAddress().isLoopbackAddress()) {
                        // avoid weird connections
                        if (networkInterface.getDisplayName().contains("wlan0") ||
                                networkInterface.getDisplayName().contains("eth0") ||
                                networkInterface.getDisplayName().contains("ap0"))
                            // add to list of possible broadcast addresses
                        if(ifAddress.getBroadcast() != null)
                            resultList.add(ifAddress.getBroadcast());
                    }
                }
            }

        } catch (SocketException ex) {
            mExceptionListener.onException(this, ex, "Broadcaster: Could not find device ip addresses");
        }
        return resultList;
    }

    /**
     * sends a self identification broadcast
     */
    @Override
    public void run() {
        // send our identification data via broadcast
        try {
            for (InetAddress address : mBroadcastAddresses)
                mDiscoveryThread.sendSelfIdentification(address, mRemotePort);
        } catch (IOException e) {
            mExceptionListener.onException(this, e, "Discovery: Broadcaster: Exception when trying to broadcast");
        }
    }
}