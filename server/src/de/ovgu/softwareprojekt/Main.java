package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.discovery.DiscoveryServer;

import java.net.SocketException;

public class Main {

    public static void main(String[] args) throws SocketException {
	// write your code here
        System.out.println("Hello World");

        DiscoveryServer discoveryServer = new DiscoveryServer(8888, "lessig. _christian_ lessig");

        discoveryServer.start();
    }
}
