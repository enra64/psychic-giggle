package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.*;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This class listens for udp packets containing {@link SensorData} objects, and notifies a listener of new data.
 * The listener must be
 */
class UdpDataConnection extends Thread implements NetworkDataSource {
    /**
     * Incoming sensor data will be forwarded here
     */
    private NetworkDataSink mDataSink;

    /**
     * true as long as the server should listen on the udp port
     */
    private boolean mKeepRunning = true;

    /**
     * the port we should listen on
     */
    private int mLocalPort;

    /**
     * Who to report bad exceptions to
     */
    private ExceptionListener mExceptionListener;

    /**
     * The client that is connected to the udp connection
     */
    private NetworkDevice mClient;


    /**
     * Create a new UdpDataConnection that will start listening after {@link #start()} is called.
     *
     * @param exceptionListener the {@link ExceptionListener} called when a (possibly threaded) exception occurs
     * @throws SocketException if no free port could be found
     */
    UdpDataConnection(ExceptionListener exceptionListener) throws SocketException {
        super("UdpDataConnection: unbound");
        mLocalPort = findFreePort();
        mExceptionListener = exceptionListener;
    }

    /**
     * Set the client that sends data to this {@link UdpDataConnection}
     * @param client the client that should send its data here
     */
    void setClient(NetworkDevice client) {
        mClient = client;

        // update thread name
        setName("UdpDataConnection for " + mClient);
    }

    /**
     * Find a free port to listen on
     *
     * @return a currently (!) free port
     * @throws SocketException when an error occurs during the search
     */
    private static int findFreePort() throws SocketException {
        DatagramSocket socket = new DatagramSocket();
        int freePort = socket.getLocalPort();
        socket.close();
        return freePort;
    }

    /**
     * Returns the port this UdpDataConnection is listening on
     *
     * @return the port this {@link UdpDataConnection} listens on
     */
    int getLocalPort() {
        return mLocalPort;
    }

    @Override
    public void run() {
        // this is a "try with resources": it automatically closes the socket, whatever happens
        try (DatagramSocket serverSocket = new DatagramSocket(mLocalPort)) {
            // storage for udp data
            byte[] appData = new byte[1024];

            while (mKeepRunning) {
                // receive udp packet
                DatagramPacket appPacket = new DatagramPacket(appData, appData.length);
                serverSocket.receive(appPacket);

                // parse incoming object
                ByteArrayInputStream input = new ByteArrayInputStream(appData);
                ObjectInputStream objectInputStream = new ObjectInputStream(input);

                // notify listener
                mDataSink.onData(mClient, (SensorData) objectInputStream.readObject(), -1);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            mExceptionListener.onException(
                    UdpDataConnection.this,
                    e,
                    "A NullPointerException was encountered when data arrived.");
        }
    }

    @Override
    public void setDataSink(NetworkDataSink sink) {
        mDataSink = sink;
    }

    @Override
    public void close() {
        mKeepRunning = false;
    }
}
