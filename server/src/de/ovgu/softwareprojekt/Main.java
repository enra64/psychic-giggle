package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.CommandSource;
import de.ovgu.softwareprojekt.control.commands.BooleanCommand;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.CommandType;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.discovery.DiscoveryServer;

import java.io.IOException;
import java.net.InetAddress;


public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // because of interface restrictions (eg CommandSource.OnCommandListener), we must work within a class
        Server server = new Server();
    }

    private static class Server implements CommandSource.OnCommandListener {
        private UdpDataConnection mDataConnection;
        private CommandConnection mCommandConnection;

        Server() throws IOException {

            // begin a new command connection
            mCommandConnection = new CommandConnection();

            // start listening to the commands received by the connection
            mCommandConnection.setCommandListener(this);
            mCommandConnection.start();

            // find a free port for the data connection
            int dataPort = UdpDataConnection.findPort();

            // begin listening for udp packets on that free port
            mDataConnection = new UdpDataConnection(dataPort);
            mDataConnection.start();

            // register a callback for data objects
            // TEMPORARY SOLUTION DO NOT PUT THE MOUSE CONTROL HERE
            mDataConnection.setDataSink(new DataSink() {
                @Override
                public void onData(SensorData data) {
                    System.out.println("received a data object!");
                }

                @Override
                public void close() {}
            });

            // bad command-line logging
            System.out.println("listening for commands on " + mCommandConnection.getLocalPort() + ", found a free port for data: " + dataPort);

            // begin listening for discovery packets on port 8888, announcing our command and data ports
            DiscoveryServer discoveryServer = new DiscoveryServer(
                    8888,
                    mCommandConnection.getLocalPort(),
                    dataPort,
                    "lessig. _christian_ lessig");
            discoveryServer.start();

            System.out.println("discovery server started");
        }

        /**
         * Called when the command connection received a command packet
         * @param origin host of the sender
         * @param command the command issued
         */
        @Override
        public void onCommand(InetAddress origin, Command command) {
            // command-line logging
            System.out.println("command received, type " + command.getCommandType().toString());

            // decide what to do with the packet
            switch(command.getCommandType()){

                // TODO: check whether we actually want to connect with this client
                case ConnectionRequest:
                    // cast to ConnectionRequest type
                    ConnectionRequest request = (ConnectionRequest) command;

                    try {
                        // because the command connectino goes two ways, we need to register the new remotes command endpoint
                        mCommandConnection.setRemote(origin, request.self.commandPort);

                        // used for debugging: let the remote begin sending gyroscope data
                        mCommandConnection.sendCommand(new BooleanCommand(CommandType.SetGyroscope, true));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                // ignore unhandled commands
                default:
                    break;
            }
        }
    }
}
