package de.ovgu.softwareprojektapp.server_discovery;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import de.ovgu.softwareprojekt.discovery.*;

/**
 * This thread manages sending discovery request packages and receiving answers
 */
public class DiscoveryThread extends de.ovgu.softwareprojekt.discovery.DiscoveryThread {
    private OnDiscoveryListener mDiscoveryListener;
    private List<DeviceIdentification> mCurrentServerList = new LinkedList<>();
    private int mRemotePort;

    public DiscoveryThread(OnDiscoveryListener listener, int remotePort, String selfName) throws SocketException {
        super(selfName);

        mRemotePort = remotePort;
        mDiscoveryListener = listener;
    }

    @Override
    public void run() {
        try {
            setSocket(new DatagramSocket());

            // send our identification data via broadcast
            sendSelfIdentification(InetAddress.getByName("255.255.255.255"), mRemotePort);


            // we want to periodically check if we still wan to run
            while (isRunning()) {
                try {
                    listen();
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (IOException ex) {
            Log.w("discovery", "IOException");
            ex.printStackTrace();
        }

        if(getSocket() != null)
            getSocket().close();
    }

    @Override
    protected void onDiscovery(DeviceIdentification deviceIdentification) {
        // only if we have not yet found this server we may add it
        if(!mCurrentServerList.contains(deviceIdentification)){
            mCurrentServerList.add(deviceIdentification);
            mDiscoveryListener.onServerListUpdated(mCurrentServerList);
        }
    }
}
