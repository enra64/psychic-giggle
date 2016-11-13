package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.CommandSource;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.discovery.DiscoveryServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        // because of interface restrictions (eg CommandSource.OnCommandListener), we must work within a class
        Server server = new Server();
    }

    private static class Server implements CommandSource.OnCommandListener {
        Server() throws IOException {
            // TODO: the command connection
            CommandConnection commandConnection = new CommandConnection();
            commandConnection.setCommandListener(this);
            commandConnection.start();

            System.out.println("listening for commands on " + commandConnection.getLocalPort());

            // look at the docs
            DiscoveryServer discoveryServer = new DiscoveryServer(8888, commandConnection.getLocalPort(), 8890, "lessig. _christian_ lessig");
            discoveryServer.start();

            System.out.println("discovery server started");
        }

        @Override
        public void onCommand(Command command) {
            System.out.println("command received, type " + command.getCommandType().toString());
        }
    }
}
