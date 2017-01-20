package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.discovery.DiscoveryThread;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Use this class to wait for incoming discovery requests
 */
class DiscoveryServer extends DiscoveryThread {
    /**
     * The port we should listen on for broadcasts by discovery clients
     */
    private int mListenPort;

    private ExceptionListener mExceptionListener;

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
     * @param exceptionListener  the listener for exceptions that could not be handled internally
     */
    DiscoveryServer(ExceptionListener exceptionListener, int localDiscoveryPort, int localCommandPort, int localDataPort, String selfName) {
        super(selfName, localCommandPort, localDataPort);
        mListenPort = localDiscoveryPort;
        mExceptionListener = exceptionListener;

        // name thread
        setName("DiscoveryServer");
    }

    /**
     * Listen for incoming discovery requests
     */
    @Override
    public void run() {
        try {
            // set our socket listen to broadcasts on the configured listen port
            setSocket(new DatagramSocket(mListenPort, InetAddress.getByName("0.0.0.0")));

            // timeout to allow for stopping the server
            getSocket().setSoTimeout(20);

            // continue running until close() is called
            while (isRunning()) {
                try {
                    listen();
                    // if the socket times out, we just want to check if we should listen again, so we ignore the exception
                } catch (SocketTimeoutException ignored) {
                }
            }

        } catch (BindException e) {
            mExceptionListener.onException(this, e, "The port seems to be in use already: " + mListenPort);
        } catch (IOException e) {
            mExceptionListener.onException(this, e, "DiscoveryServer: could not listen on 0.0.0.0:" + mListenPort);
        } finally {
            // after we finished running, our socket must be closed
            if (getSocket() != null)
                getSocket().close();
        }

    }

    /**
     * Stop the listening loop, and wait until the socket is closed.
     */
    @Override
    public void close() {
        super.close();
        // wait until the socket is closed
        while (!getSocket().isClosed()) ;
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