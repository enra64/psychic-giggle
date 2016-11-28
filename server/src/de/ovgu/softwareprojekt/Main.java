package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.servers.mouse.MouseServer;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        new MouseServer(null).start();
    }
}
