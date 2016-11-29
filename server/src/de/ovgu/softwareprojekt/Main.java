package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.servers.mouse.MouseServer;

import java.awt.*;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException, AWTException {
        new MouseServer(null).start();
    }
}
