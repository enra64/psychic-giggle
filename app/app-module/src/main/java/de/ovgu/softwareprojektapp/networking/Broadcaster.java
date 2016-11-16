package de.ovgu.softwareprojektapp.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.TimerTask;

import de.ovgu.softwareprojekt.discovery.DiscoveryThread;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import static android.content.ContentValues.TAG;

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
    private final InetAddress mBroadcastAddress;

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

        InetAddress i = getIpAddress();


        mBroadcastAddress = getBroadcast(i);
    }

    /**
     * Find an ip address of this device
     */
    private InetAddress getIpAddress() {
        InetAddress myAddr = null;

        try {
            // iterate over all network interface
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // iterate over all ip addresses this network interface has
                Enumeration<InetAddress> IpAddresses = networkInterface.getInetAddresses();
                while (IpAddresses.hasMoreElements()) {
                    InetAddress ipAddress = IpAddresses.nextElement();

                    // check if this is an eligible ip address: no loopback and either wlan ethernet or access point
                    if (!ipAddress.isLoopbackAddress() &&
                            (networkInterface.getDisplayName().contains("wlan0") ||
                                    networkInterface.getDisplayName().contains("eth0") ||
                                    networkInterface.getDisplayName().contains("ap0"))) {
                        myAddr = ipAddress;
                    }
                }
            }

        } catch (SocketException ex) {
            mExceptionListener.onException(this, ex, "Broadcaster: Could not find broadcast address");
        }
        return myAddr;
    }

    /**
     * Get the broadcast address for an ip address
     */
    private InetAddress getBroadcast(InetAddress inetAddress) {

        try {
            NetworkInterface temp = NetworkInterface.getByInetAddress(inetAddress);
            List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

            InetAddress broadcastAddress = null;
            for (InterfaceAddress tmpAddr : addresses)
                broadcastAddress = tmpAddr.getBroadcast();
            return broadcastAddress;

        } catch (SocketException e) {
            mExceptionListener.onException(this, e, "Broadcaster: Could not find broadcast address");
        }
        return null;
    }

    /**
     * sends a self identification broadcast
     */
    @Override
    public void run() {
        // send our identification data via broadcast
        try {
            mDiscoveryThread.sendSelfIdentification(mBroadcastAddress, mRemotePort);
        } catch (IOException e) {
            mExceptionListener.onException(this, e, "Discovery: Broadcaster: Exception when trying to broadcast");
        }
    }
}