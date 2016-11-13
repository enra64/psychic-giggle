package de.ovgu.softwareprojektapp.networking;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;

/**
 * The UdpConnection class is an UDP implementation of the DataSink interface; it sends all input
 * data to the host and port given in the constructor
 */

public class UdpConnection implements DataSink {
    /**
     * The socket we use for sending data; initialized in the constructor
     */
    private DatagramSocket mSocket;

    /**
     * The target host for our UDP packets
     */
    private InetAddress mHost;

    /**
     * The target port on {@link #mHost} for our UDP packets
     */
    private final int mPort;

    /**
     * Initialize the connection using specified port and host
     */
    public UdpConnection(InetAddress host, int port) throws IOException {
        // save host(translated) and port
        mPort = port;
        mHost = host;
        mSocket = new DatagramSocket();
    }

    public int getLocalPort(){
        return mSocket.getLocalPort();
    }

    /**
     * call before exiting
     */
    @Override
    public void close() {
        mSocket.close();
    }

    /**
     * Push new data to the remote partner
     */
    @Override
    public void onData(SensorData sensorData) {
        new SensorOut().execute(sensorData);
    }


    /**
     * Class to encapsulate sending SensorData objects asynchronously
     */
    private class SensorOut extends AsyncTask<SensorData, Void, Boolean> {

        @Override
        protected Boolean doInBackground(SensorData... sensorData) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(sensorData[0]);
                byte[] data = outputStream.toByteArray();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, mHost, mPort);
                mSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }
}