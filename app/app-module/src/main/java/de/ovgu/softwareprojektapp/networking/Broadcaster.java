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
     * @param context         required for wifi state analysis
     * @param discoveryThread required for {@link DiscoveryThread#sendSelfIdentification(InetAddress, int)}
     * @param remotePort      required for sending broadcasts
     * @throws IOException when we cannot detect the correct broadcast address
     */
    Broadcaster(Context context, DiscoveryThread discoveryThread, ExceptionListener exceptionListener, int remotePort) throws IOException {
        mRemotePort = remotePort;
        mDiscoveryThread = discoveryThread;
        mExceptionListener = exceptionListener;

        InetAddress i = getIpAddress();


        mBroadcastAddress = getBroadcast(i);
    }

    public InetAddress getIpAddress() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements(); ) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements(); ) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {

                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return myAddr;
    }

    private InetAddress getBroadcast(InetAddress inetAddr) {

        NetworkInterface temp;
        InetAddress iAddr = null;
        try {
            temp = NetworkInterface.getByInetAddress(inetAddr);
            List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

            for (InterfaceAddress inetAddress : addresses)

                iAddr = inetAddress.getBroadcast();
            Log.d(TAG, "iAddr=" + iAddr);
            return iAddr;

        } catch (SocketException e) {

            e.printStackTrace();
            Log.d(TAG, "getBroadcast" + e.getMessage());
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