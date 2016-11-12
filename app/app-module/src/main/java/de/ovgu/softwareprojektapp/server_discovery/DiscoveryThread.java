package de.ovgu.softwareprojektapp.server_discovery;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import de.ovgu.softwareprojekt.discovery.ServerIdentification;

/**
 * This thread manages sending discovery request packages and receiving answers
 */
class DiscoveryThread extends Thread {
    private int mPort;
    private DatagramSocket mSocket;
    private DiscoveryListener mListener;
    private boolean mKeepRunning = true;

    /**
     * Called when the {@link DiscoveryThread} gets a discovery response
     */
    interface DiscoveryListener {

        /**
         * Called when the {@link DiscoveryThread} gets a discovery response
         * @param server server that was found; not unique
         */
        void onDiscovery(Server server);
    }

    /**
     * Create a new {@link DiscoveryThread}; use {@link #start()} to begin network operations
     * @param listener callback listener for server responses
     * @param port port that requests will be sent to
     */
    DiscoveryThread(DiscoveryListener listener, int port) {
        mListener = listener;
        mPort = port;
    }

    /**
     * This will try and find servers on the network.
     * TODO: send more than one packet if no response is registered
     */
    @Override
    public void run() {
        try {
            // Create a broadcast UDP socket with a timeout of 50ms
            mSocket = new DatagramSocket();
            mSocket.setBroadcast(true);
            mSocket.setSoTimeout(50);

            byte[] sendData = "DE_OVGU_SOFTWAREPROJEKT_SERVER_DISCOVERY_REQUEST".getBytes();

            // send an UDP packet to the standard broadcast address
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, InetAddress.getByName("255.255.255.255"), mPort);
            mSocket.send(sendPacket);

            // we want to periodically check if we still wan to run
            while (mKeepRunning) {
                try {
                    listen();
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (IOException ex) {
            Log.w("discovery", "IOException");
            ex.printStackTrace();
        }
        mSocket.close();
    }

    /**
     * Listen for a discovery response packet
     *
     * @throws IOException will throw a {@link java.net.SocketTimeoutException} if a timeout occurs
     *                     while waiting
     */
    private void listen() throws IOException {
        // wait for a response
        byte[] recvBuf = new byte[15000];
        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
        mSocket.receive(receivePacket);

        // convert to object
        ByteArrayInputStream input = new ByteArrayInputStream(recvBuf);
        ObjectInputStream oinput = new ObjectInputStream(input);
        ServerIdentification serverIdentification;

        // we assume here that a server sending us our object is trying to communicate with us,
        // so if casting to a ServerIdentification object does not throw, we can announce the server
        try {
            serverIdentification = (ServerIdentification) oinput.readObject();
            mListener.onDiscovery(new Server(serverIdentification, receivePacket.getAddress()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Signal the running while loop to stop
     */
    void close() {
        mKeepRunning = false;
    }
}
