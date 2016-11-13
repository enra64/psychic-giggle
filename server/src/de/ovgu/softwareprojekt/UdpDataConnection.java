package de.ovgu.softwareprojekt;

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
class UdpDataConnection extends Thread implements DataSource {
    /**
     * Incoming sensor data will be forwareded here
     */
    private DataSink mDataSink;

    /**
     * true as long as the server should listen on the udp port
     */
    private boolean mKeepRunning = true;

    /**
     * the port we should listen on
     */
    private int mLocalPort;

    /**
     * Create a new UdpDataConnection that will start listening after {@link #start()} is called.
     */
    UdpDataConnection() throws SocketException {
        mLocalPort = findFreePort();
    }

    /**
     * Find a free port to listen on
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
     */
    public int getLocalPort(){
        return mLocalPort;
    }

    @Override
    public void run() {
        // this is a "try with resources": it automatically closes the socket, whatever happens
        try(DatagramSocket serverSocket = new DatagramSocket(mLocalPort)) {
            // if we dont fuck up, we should not need this
            //serverSocket.setReuseAddress(true);

            // storage for udp data
            byte[] appData = new byte[1024];

            while (mKeepRunning) {
                // receive udp packet
                DatagramPacket appPacket = new DatagramPacket(appData, appData.length);
                serverSocket.receive(appPacket);

                // parse incoming object
                ByteArrayInputStream input = new ByteArrayInputStream(appData);
                ObjectInputStream oinput = new ObjectInputStream(input);

                // notify listener
                mDataSink.onData((SensorData) oinput.readObject());
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDataSink(DataSink sink) {
        mDataSink = sink;
    }

    @Override
    public void close() {
        mKeepRunning = false;
    }
}
