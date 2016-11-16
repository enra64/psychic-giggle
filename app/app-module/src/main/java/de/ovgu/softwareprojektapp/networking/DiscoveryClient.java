package de.ovgu.softwareprojektapp.networking;

import android.content.Context;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.ovgu.softwareprojekt.discovery.*;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

/**
 * This thread manages sending discovery request packages and receiving answers from servers
 */
public class DiscoveryClient extends DiscoveryThread {
    /**
     * Listener to be called when our list of servers changes
     */
    private OnDiscoveryListener mDiscoveryListener;

    /**
     * Interface for listening to exceptions. Exceptions occurring in a multi-threaded environment can
     * be easily handled through this.
     */
    private ExceptionListener mExceptionListener;

    /**
     * Current list of known servers
     */
    private List<NetworkDevice> mCurrentServerList = new LinkedList<>();

    /**
     * Our broadcaster handles the recurring self identification broadcasts.
     */
    private Broadcaster mBroadcaster;

    /**
     * Timer used to schedule recurring broadcasts
     */
    private Timer mRecurringBroadcastTimer = new Timer();

    /**
     * Create a new DiscoveryClient. In contrast to the DiscoveryServer, the DiscoveryClient does not
     * have to know its command- and data port yet, because we can instantiate those when acutally
     * connecting to the ports supplied in the discovery response sent by the sever.
     *
     * @param listener   this listener will be notified of changes in the list of known servers
     * @param remotePort the port the discovery server listens on
     * @param selfName   the name this device should announce itself as
     */
    public DiscoveryClient(OnDiscoveryListener listener, ExceptionListener exceptionListener, Context context, int remotePort, String selfName) throws IOException {
        super(selfName);

        // save who wants to be notified of new servers
        mDiscoveryListener = listener;

        // save who wants to be notified of exceptions
        mExceptionListener = exceptionListener;

        // the broadcaster handles everything related to sending broadcasts
        mBroadcaster = new Broadcaster(context, this, mExceptionListener, remotePort);
    }

    /**
     * Broadcast our information into the network, and listen to responding servers
     */
    @Override
    public void run() {
        try {
            setSocket(new DatagramSocket());

            // send a new broadcast every four seconds, beginning now
            mRecurringBroadcastTimer.scheduleAtFixedRate(mBroadcaster, 0, 4000);

            // continously check if we should continue listening
            while (isRunning()) {
                try {
                    listen();
                    // ignore timeoutexceptions, they are necessary to be able to check isRunning()
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // close the socket after use
        if (getSocket() != null)
            getSocket().close();
    }

    /**
     * When a server is discovered, check if we already know it; if not, add it to the list, and notify
     * our listener.
     *
     * @param device the newly found device
     */
    @Override
    protected void onDiscovery(NetworkDevice device) {
        // only if we have not yet found this server we may add it to our list of servers
        if (!mCurrentServerList.contains(device)) {
            mCurrentServerList.add(device);
            mDiscoveryListener.onServerListUpdated(mCurrentServerList);
        }
    }

    @Override
    public void close() {
        // stop the listening loop
        super.close();

        // stop sending broadcasts
        mRecurringBroadcastTimer.cancel();
    }


}
