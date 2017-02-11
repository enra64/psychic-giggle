package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.examples.gui.ServerSelectionWindow;
import de.ovgu.softwareprojekt.examples.kuka.KukaServer;
import de.ovgu.softwareprojekt.examples.mouse.MouseServer;
import de.ovgu.softwareprojekt.examples.nes.NesServer;


import java.awt.*;
import java.io.IOException;

/**
 * Main class for switching between the different example uses for the framework.
 */
public class Main {
    public static void main(String[] args) throws IOException, AWTException {
        switch(args.length > 0 ? args[0] : ""){
            case "mouse":
                new MouseServer(null).start();
                System.out.println("MouseServer started");
                break;
            case "vrep":
                new KukaServer().start();
                System.out.println("KukaServer started");
                break;
            case "nes":
                new NesServer(null).start();
                System.out.println("NesServer started");
                break;
            default:
                ServerSelectionWindow.start();
                System.out.println("GUI started");
                break;
        }
    }
}