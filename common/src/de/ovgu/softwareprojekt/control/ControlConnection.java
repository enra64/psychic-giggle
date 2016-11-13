package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.Command;

import javax.naming.ldap.Control;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class ControlConnection implements CommandSink, CommandSource {
    /**
     * This listener must be notified of new commands arriving
     */
    private OnCommandListener mCommandListener;

    /**
     * The CommandServer is responsible for listening to incoming commands
     */
    private CommandServer mIncomingServer;

    /**
     * This host should receive the outbound commands
     */
    private InetAddress mRemoteHost;

    /**
     * This is the port where outbound commands should arrive at {@link #mRemoteHost}
     */
    private int mRemotePort;

    /**
     * This is the port where incoming commands are expected to arrive
     */
    private int mLocalPort;

    /**
     * New control connection
     *
     * @param remoteHost         This host should receive the outbound commands
     * @param remotePort         This is the port where outbound commands should arrive at remoteHost
     * @param localListeningPort This is the port where incoming commands are expected to arrive
     */
    public ControlConnection(InetAddress remoteHost, int remotePort, int localListeningPort) {
        mLocalPort = localListeningPort;
        mRemoteHost = remoteHost;
        mRemotePort = remotePort;
    }

    /**
     * Use this function to send a command to the peer
     *
     * @param command this command will be received by the peer
     * @throws IOException most probably if the socket is blocked TODO: definitive answer here
     */
    @Override
    public void inputCommand(Command command) throws IOException {
        // this is a try-with-resources; it will automatically close its resources when finished, whatever happens.
        try (Socket outboundSocket = new Socket(mRemoteHost, mRemotePort);
             ObjectOutputStream oos = new ObjectOutputStream(outboundSocket.getOutputStream())) {
            oos.writeObject(command);
        }
    }

    /**
     * Use this to gracefully stop operations
     */
    @Override
    public void close() {
        if(mIncomingServer != null)
            mIncomingServer.shutdown();
        mIncomingServer = null;
    }

    /**
     * Set the listener that will be notified of new commands arriving at this ControlConnection
     *
     * @param listener the listener that will be notified of new commands
     */
    @Override
    public void setCommandListener(OnCommandListener listener) {
        mCommandListener = listener;
    }

    /**
     * Use this to start operations. Does not throw if called multiple times.
     */
    @Override
    public void start() {
        if(mIncomingServer == null){
            mIncomingServer = new CommandServer(this, mLocalPort);
            mIncomingServer.start();
        }
    }

    /**
     * Called whenever our CommandServer receives a command packet.
     *
     * @param command the command we received
     */
    // this could have been implemented by ControlConnection implementing the OnCommandListener, but because it is an
    // interface, it would have polluted our public surface with a method that is destined to be used by inner class
    // workings only.
    private void onCommand(Command command) {
        mCommandListener.onCommand(command);
    }

    /**
     * The CommandServer class is the actual workhorse of the TcpServer class;
     */
    private static class CommandServer extends Thread {
        /**
         * Our listener; called whenever a new SensorData object is received
         */
        ControlConnection mListener;

        /**
         * true if the server should continue running
         */
        private boolean mRunning = true;

        /**
         * The port we should listen on
         */
        private int mListeningPort;

        CommandServer(ControlConnection listener, int listenPort) {
            mListener = listener;
            mListeningPort = listenPort;
        }

        /**
         * Gracefully stop the server asap
         */
        void shutdown() {
            // kill the running server
            mRunning = false;
        }

        /**
         * Listen on socket;
         * TODO: exception listener to handle exceptions in the thread more intelligently than "hopefully someone sees this just died"
         */
        public void run() {
            // declare objects that will need to be closed
            ObjectInputStream oinput = null;

            try(ServerSocket listenerSocket = new ServerSocket(mListeningPort);
                Socket connection = listenerSocket.accept()){

                // notify user of working state
                System.out.println("command connection on port " + listenerSocket.getLocalPort());

                // get an object input stream; this is open as long as connection is open,
                // so the tcp connection stays open as long as Data is transmitted
                oinput = new ObjectInputStream(connection.getInputStream());

                // read objects on the same connection until shutdown() is called
                while (mRunning) {
                    // call listener with new object
                    mListener.onCommand((Command) oinput.readUnshared());
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
