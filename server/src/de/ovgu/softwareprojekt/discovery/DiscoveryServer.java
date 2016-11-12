package de.ovgu.softwareprojekt.discovery;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DiscoveryServer extends DiscoveryThread {
    private int mListenPort;

    public DiscoveryServer(int localPort, String selfName) throws SocketException {
        super(selfName);
        mListenPort = localPort;
    }

    @Override
    public void run() {
        // Create a broadcast UDP socket with a timeout of 50ms
        try {
            setSocket(new DatagramSocket(mListenPort, InetAddress.getByName("0.0.0.0")));

            while(isRunning()){
                listen();
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getSocket().close();
    }

    @Override
    protected void onDiscovery(DeviceIdentification device) {
        try {
            // reply to the device
            sendSelfIdentification(device.getInetAddress(), device.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}