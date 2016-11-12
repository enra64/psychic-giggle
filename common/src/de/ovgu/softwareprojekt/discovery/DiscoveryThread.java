package de.ovgu.softwareprojekt.discovery;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import sun.rmi.runtime.Log;

/**
 * This thread manages sending discovery request packages and receiving answers
 */
public abstract class DiscoveryThread extends Thread {
    private DatagramSocket mSocket;
    private boolean mKeepRunning = true;
    private DeviceIdentification mSelfDeviceId;

    public DiscoveryThread(String selfName) throws SocketException {
        mSelfDeviceId = new DeviceIdentification(selfName);
    }

    /**
     * This will try and find servers on the network.
     * TODO: send more than one packet if no response is registered
     */
    @Override
    public abstract void run();

    protected void sendSelfIdentification(InetAddress targetHost, int targetPort) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(mSelfDeviceId) ;
        byte[] data = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, targetHost, targetPort);
        mSocket.send(sendPacket);
    }

    protected void listen() throws IOException {
        // wait for a response
        byte[] recvBuf = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
        mSocket.receive(receivePacket);

        // convert to object
        ByteArrayInputStream input = new ByteArrayInputStream(recvBuf);
        ObjectInputStream oinput = new ObjectInputStream(input);
        DeviceIdentification identification;

        // we assume here that a server sending us our object is trying to communicate with us,
        // so if casting to a ServerIdentification object does not throw, we can announce the server
        try {
            identification = (DeviceIdentification) oinput.readObject();
            identification.address = receivePacket.getAddress().getHostAddress();

            onDiscovery(identification);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected abstract void onDiscovery(DeviceIdentification newDevice);


    public boolean isRunning() {
        return mKeepRunning;
    }

    protected DatagramSocket getSocket(){
        return mSocket;
    }

    protected void setSocket(DatagramSocket socket) throws SocketException {
        socket.setBroadcast(true);
        mSocket = socket;

        // the local port may be unknown at time of
        mSelfDeviceId.port = socket.getLocalPort();
    }

    /**
     * Signal the running while loop to stop
     * TODO: what happens if we close, but want to restart?
     */
    public void close() {
        mKeepRunning = false;
    }
}
