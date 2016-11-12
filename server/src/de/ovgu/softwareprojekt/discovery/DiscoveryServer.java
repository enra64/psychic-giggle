package de.ovgu.softwareprojekt.discovery;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
     * call {@link #close()} after finishing the discovery phase
     *
     * @param localPort the local listening port. remotes must know it to find this server
     * @param selfName how to announce this server to any remotes
     */
    public DiscoveryServer(int localPort, String selfName) {
        super(selfName);
        mListenPort = localPort;
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
     * @param device the device that was found
     */
    @Override
    protected void onDiscovery(NetworkDevice device) {
        try {
            // reply to the device
            sendSelfIdentification(device.getInetAddress(), device.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}