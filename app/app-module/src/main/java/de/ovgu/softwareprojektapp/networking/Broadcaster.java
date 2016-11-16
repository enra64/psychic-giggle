package de.ovgu.softwareprojektapp.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
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
     * @param context required for wifi state analysis
     * @param discoveryThread required for {@link DiscoveryThread#sendSelfIdentification(InetAddress, int)}
     * @param remotePort required for sending broadcasts
     * @throws IOException when we cannot detect the correct broadcast address
     */
    Broadcaster(Context context, DiscoveryThread discoveryThread, ExceptionListener exceptionListener, int remotePort) throws IOException {
        mRemotePort = remotePort;
        mDiscoveryThread = discoveryThread;
        mExceptionListener = exceptionListener;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mBroadcastAddress = getBroadcastAddress(wifiManager, getCodecIpAddress(wifiManager, getNetworkInfo(context)));
    }

    private InetAddress getBroadcastAddress(WifiManager wm, int ipAddress) throws IOException {
        DhcpInfo dhcp = wm.getDhcpInfo();
        if (dhcp == null)
            return InetAddress.getByName("255.255.255.255");
        int broadcast = (ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    /**
     * Get a current NetworkInfo object from android
     */
    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private static int getCodecIpAddress(WifiManager wm, NetworkInfo wifi) {
        WifiInfo wi = wm.getConnectionInfo();
        if (wifi.isConnected())
            return wi.getIpAddress(); //normal wifi
        Method method = null;
        try {
            method = wm.getClass().getDeclaredMethod("getWifiApState");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method != null)
            method.setAccessible(true);
        int actualState = -1;
        try {
            if (method != null)
                actualState = (Integer) method.invoke(wm, (Object[]) null);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (actualState == 13) {  //if wifiAP is enabled
            return convertIP2Int(new byte[]{(byte) 192, (byte) 168, 43, 1}); //hardcoded WifiAP ip
        }
        return 0;
    }

    /**
     * Converts a byte array to an integer representation of an ip address, e.g.
     *
     * 192.168.0.1 is {192, 168, 0, 1} as an argument
     */
    private static int convertIP2Int(byte[] ipAddress) {
        return (int) (
                Math.pow(256, 3) * (ipAddress[3] & 0xFF) +
                        Math.pow(256, 2) * (ipAddress[2] & 0xFF) +
                        256 * (ipAddress[1] & 0xFF) +
                        (ipAddress[0] & 0xFF));
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