package de.ovgu.softwareprojekt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpDataConnection extends Thread implements DataSource {
    private DataSink mDataSink;
    private boolean mKeepRunning = true;
    private int mLocalPort;

    public UdpDataConnection(int port){
        mLocalPort = port;
    }

    public static int findPort() throws SocketException {
        DatagramSocket socket = new DatagramSocket();
        int freePort = socket.getLocalPort();
        socket.close();
        return freePort;
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

                // parse incoming SensorData object
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
