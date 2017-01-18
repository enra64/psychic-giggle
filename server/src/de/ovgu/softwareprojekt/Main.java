package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.servers.kuka.vrep.VrepServer;
import de.ovgu.softwareprojekt.servers.mouse.MouseServer;
import de.ovgu.softwareprojekt.servers.nes.NesServer;


import java.awt.*;
import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException, AWTException {
        switch(args.length > 0 ? args[0] : ""){
            case "mouse":
                new MouseServer(null).start();
                System.out.println("MouseServer started");
                break;
            case "vrep":
                new VrepServer().start();
                System.out.println("VrepServer started");
            default:
            case "nes":
                new NesServer(null).start();
                System.out.println("NesServer started");
                break;
        }
    }
}