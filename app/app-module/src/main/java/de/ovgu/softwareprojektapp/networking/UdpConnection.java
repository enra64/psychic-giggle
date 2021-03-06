package de.ovgu.softwareprojektapp.networking;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

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
    private int mPort;

    /**
     * This exception listener is to be notified of exceptions here
     */
    private ExceptionListener mExceptionListener;

    /**
     * Initialize the connection using specified port and host
     */
    public UdpConnection(NetworkDevice server, ExceptionListener listener) throws IOException {
        // save host(translated) and port
        mPort = server.dataPort;
        mHost = server.getInetAddress();
        mSocket = new DatagramSocket();
        mExceptionListener = listener;
    }

    /**
     * Get the port the udp connection uses locally
     * @return the port the udp connection uses locally
     */
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
        if(!mSocket.isClosed())
            new SensorOut().execute(sensorData);
        else
            Log.w("PsychicApp", "You are trying to send data on a closed connection!");
    }

    /**
     * Immediately change the port all data will be sent to
     * @param remotePort the new port for data to arrive at
     */
    void setRemotePort(int remotePort) {
        mPort = remotePort;
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

                //long start = System.nanoTime(); // benchmarking
                //System.out.println(System.nanoTime() - start); // benchmarking

                DatagramPacket sendPacket = new DatagramPacket(data, data.length, mHost, mPort);
                mSocket.send(sendPacket);
            } catch (IOException e) {
                mExceptionListener.onException(UdpConnection.this, e, "UdpConnection: could not send SensorData object");
                return false;
            }

            return true;
        }
    }
}