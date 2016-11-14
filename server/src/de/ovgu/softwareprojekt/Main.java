package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.control.commands.ConnectionRequest;
import de.ovgu.softwareprojekt.control.commands.SetSensorCommand;
import de.ovgu.softwareprojekt.discovery.DiscoveryServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;


public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // because of interface restrictions (eg CommandSource.OnCommandListener), we must work within a class
        Server server = new Server();
    }

    /**
     * This class encapsulates the whole server system:
     * <p>
     * 1) A DiscoveryServer used to make clients able to find us
     * 2) A CommandConnection to be able to reliable communicate about important stuff, like enabling sensors
     * 3) A DataConnection to rapidly transmit sensor data
     */
    private static class Server implements OnCommandListener {
        /**
         * Useful when you want to transmit SensorData
         */
        private UdpDataConnection mDataConnection;

        /**
         * Two-way communication for basically everything non-data, like enabling sensors or requesting connections
         */
        private CommandConnection mCommandConnection;

        /**
         * Creates a new server object
         *
         * @throws IOException when something goes wrong...
         */
        Server() throws IOException {
            // initialise the command and data connections
            initialiseCommandConnection();
            initialiseDataConnection();

            // the DiscoveryServer makes it possible for the client to find us, but it needs to know the command and
            // data ports, which is why we had to initialise those connections first
            DiscoveryServer discoveryServer = new DiscoveryServer(
                    8888,
                    mCommandConnection.getLocalPort(),
                    mDataConnection.getLocalPort(),
                    "lessig. _christian_ lessig");
            discoveryServer.start();

            System.out.println("discovery server started");
        }

        /**
         * Called whenever a command packet has arrived
         *
         * @param origin  host of the sender
         * @param command the command issued
         */
        @Override
        public void onCommand(InetAddress origin, Command command) {
            // command-line logging
            System.out.println("command received, type " + command.getCommandType().toString());

            // decide what to do with the packet
            switch (command.getCommandType()) {
                case ConnectionRequest:
                    // TODO: check whether we actually want to connect with this client
                    // since this commands CommandType is ConnectionRequest, we know to what to cast it
                    ConnectionRequest request = (ConnectionRequest) command;

                    try {
                        // now that we have a connection, we know who to talk to for the commands
                        mCommandConnection.setRemote(origin, request.self.commandPort);

                        // for now, immediately let the client begin sending gyroscope data
                        mCommandConnection.sendCommand(new SetSensorCommand(SensorType.Gyroscope, true));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                // ignore unhandled commands (like SetSensorCommand)
                default:
                    break;
            }
        }

        /**
         * Simply prepare the command connection and register us as listener
         */
        private void initialiseCommandConnection() throws IOException {
            // begin a new command connection, set this as callback
            mCommandConnection = new CommandConnection(this);

            // begin listening for commands
            mCommandConnection.start();
        }

        /**
         * Prepare the data connection, and create a listener.
         */
        private void initialiseDataConnection() throws SocketException {
            // begin a new data connection
            mDataConnection = new UdpDataConnection();

            // register a callback for data objects
            mDataConnection.setDataSink(new DataSink() {
                @Override
                // TODO: you probably want to listen here with the mousemover
                public void onData(SensorData data) {
                    System.out.println("received a data object!");
                }

                @Override
                public void close() {
                }
            });

            // begin listening for SensorData objects
            mDataConnection.start();
        }
    }
}
