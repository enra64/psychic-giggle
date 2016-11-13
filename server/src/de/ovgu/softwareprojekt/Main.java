package de.ovgu.softwareprojekt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
	// write your code here
        DatagramSocket serverSocket = new DatagramSocket(null);
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(25456));
        boolean connected = false;

        try {
            // create result csv, write header

            // storage for udp data
            byte[] appData = new byte[1024];

            System.out.println("running UDP server");

            while (true) {
                // receive udp packet
                DatagramPacket appPacket = new DatagramPacket(appData, appData.length);

                serverSocket.receive(appPacket);

                if(connected==false){
                    System.out.println("Connected");
                }

                connected = true;

                // parse incoming SensorData object
                ByteArrayInputStream input = new ByteArrayInputStream(appData);
                ObjectInputStream oinput = new ObjectInputStream(input);
                SensorData sensorData = (SensorData) oinput.readObject();


            }

        } finally {
            serverSocket.close();
        }

    }
}
