package de.ovgu.softwareprojektapp.networking;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import de.ovgu.softwareprojekt.discovery.DiscoveryThread;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.discovery.OnDiscoveryListener;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

/**
 * This thread manages sending discovery request packages and receiving answers from servers.
 */
public class DiscoveryClient extends DiscoveryThread {


    /**
     * Our broadcaster handles the recurring self identification broadcasts.
     */
    private Broadcaster mBroadcaster;

    /**
     * Timer used to schedule recurring broadcasts
     */
    private Timer mRecurringBroadcastTimer = new Timer();

    /**
     * Timer used to keep the list of servers up to date by regularly removing old servers
     */
    private Timer mServerListTimer = new Timer();

    /**
     * list of servers
     */
    private ServerList mCurrentServerList;

    /**
     * Create a new DiscoveryClient. In contrast to the DiscoveryServer, the DiscoveryClient does not
     * have to know its command- and data port yet, because we can instantiate those when acutally
     * connecting to the ports supplied in the discovery response sent by the sever.
     *
     * @param listener   this listener will be notified of changes in the list of known servers
     * @param remotePort the port the discovery server listens on
     * @param selfName   the name this device should announce itself as
     */
    public DiscoveryClient(OnDiscoveryListener listener, ExceptionListener exceptionListener, int remotePort, String selfName) throws IOException {
        super(selfName);

        // save who wants to be notified of new servers
        mCurrentServerList = new ServerList(listener);

        // the broadcaster handles everything related to sending broadcasts
        mBroadcaster = new Broadcaster(this, exceptionListener, remotePort);
    }

    /**
     * Broadcast our information into the network, and listen to responding servers
     */
    @Override
    public void run() {
        try {
            setSocket(new DatagramSocket());

            // send a new broadcast every four seconds, beginning now
            mRecurringBroadcastTimer.scheduleAtFixedRate(mBroadcaster, 0, 1000);
            mServerListTimer.scheduleAtFixedRate(mCurrentServerList, 100, 1000);

            // continously check if we should continue listening
            while (isRunning()) {
                try {
                    listen();
                    // ignore timeoutexceptions, they are necessary to be able to check started()
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
        mCurrentServerList.addServer(device);
    }

    @Override
    public void close() {
        // stop the listening loop
        super.close();

        // stop sending broadcasts
        mRecurringBroadcastTimer.cancel();

        // stop updating the server list
        mServerListTimer.cancel();
    }

    /**
     * A TimerTask extension that removes old servers
     */
    private class ServerList extends TimerTask {
        /**
         * Constant specifying how old a server discovery may be before it is regarded offline
         */
        private static final long MAXIMUM_SERVER_AGE_MS = 2000;

        /**
         * Current list of known servers as well as a timestamp marking their time of discovery
         */
        private ConcurrentHashMap<NetworkDevice, Long> mCurrentServerList = new ConcurrentHashMap<>();

        /**
         * Listener to be called when our list of servers is updated
         */
        private OnDiscoveryListener mDiscoveryListener;

        /**
         * Create a new ServerList that will push the most current list to the discovery listener
         * at regular intervals
         */
        ServerList(OnDiscoveryListener onDiscoveryListener) {
            mDiscoveryListener = onDiscoveryListener;
        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();

            // remove all servers older than MAXIMUM_SERVER_AGE_MS
            Iterator<Map.Entry<NetworkDevice, Long>> it = mCurrentServerList.entrySet().iterator();
            while (it.hasNext()) {
                // get the next devices discovery timestamp
                long discoveryTimestamp = it.next().getValue();

                // if the device is too old, remove it
                if ((now - discoveryTimestamp) > MAXIMUM_SERVER_AGE_MS)
                    it.remove();
            }

            // notify listener of update
            mDiscoveryListener.onServerListUpdated(getCurrentServers());
        }

        /**
         * Retrieve an up-to-date list of known servers
         */
        private List<NetworkDevice> getCurrentServers() {
            return new ArrayList<>(mCurrentServerList.keySet());
        }

        /**
         * Adds a server to the list or, if it already exists, updates it.
         */
        void addServer(NetworkDevice device) {
            mCurrentServerList.put(device, System.currentTimeMillis());
        }
    }
}
