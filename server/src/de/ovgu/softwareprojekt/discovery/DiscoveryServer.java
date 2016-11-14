package de.ovgu.softwareprojekt.discovery;

import de.ovgu.softwareprojekt.UdpDataConnection;
import de.ovgu.softwareprojekt.control.CommandConnection;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Use this class to wait for incoming discovery requests
 */
public class DiscoveryServer extends DiscoveryThread {
    /**
     * The port we should listen on for broadcasts by discovery clients
     */
    private int mListenPort;

    /**
     * Create a new discovery server. The {@link #start()} function must be called in order to begin listening. Please
     * call {@link #close()} after finishing the discovery phase. This *only* answers incoming discovery requests with our
     * device identification.
     *
     * @param localDiscoveryPort the local listening port. The only port that must be configured manually, as the remotes
     *                           must be able to send data here without having had contact first
     * @param localCommandPort   this port will be announced to any potential remote in the discovery phase. The correct
     *                           parameter can be most easily retrieved by requesting {@link CommandConnection#getLocalPort()}
     * @param localDataPort      this port will be announced to any potential remote in the discovery phase. The correct
     *                           parameter can be most easily retrieved via {@link UdpDataConnection#getLocalPort()}.
     * @param selfName           how to announce this server to any remotes
     */
    public DiscoveryServer(int localDiscoveryPort, int localCommandPort, int localDataPort, String selfName) {
        super(selfName, localCommandPort, localDataPort);
        mListenPort = localDiscoveryPort;
    }

    /**
     * Listen for incoming discovery requests
     */
    @Override
    public void run() {
        try {
            // set our socket listen to broadcasts on the configured listen port
            setSocket(new DatagramSocket(mListenPort, InetAddress.getByName("0.0.0.0")));

            // continue running until close() is called
            while (isRunning()) {
                listen();
            }

            // TODO: do something intelligent with exceptions
        } catch (IOException e) {
            e.printStackTrace();
        }

        // after we finished running, our socket must be closed
        getSocket().close();
    }

    /**
     * When a new device announces itself, we respond with the information necessary to connect to this server
     *
     * @param device the device that was found
     */
    @Override
    protected void onDiscovery(NetworkDevice device) {
        try {
            // reply to the devices discoveryPort, as the other ports will not be able to parse this message
            sendSelfIdentification(device.getInetAddress(), device.discoveryPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}