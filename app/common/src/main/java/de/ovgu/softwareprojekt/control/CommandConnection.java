package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * <p>
 * This class is able to both send and receive commands.
 * </p>
 * <p>
 * To receive commands with it, you must call {@link #start()} to start listening.
 * </p>
 * <p>
 * When you want to send commands with it, you need to configure the remote host using {@link #setRemote(InetAddress, int)}
 * first. Then you can use {@link #sendCommand(AbstractCommand)} to send any command.
 * </p>
 */
@SuppressWarnings("WeakerAccess")
public class CommandConnection {
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
    private InetAddress mRemoteHost = null;

    /**
     * This is the discoveryPort where outbound commands should arrive at {@link #mRemoteHost}
     */
    private int mRemotePort = -1;

    /**
     * New control connection. The local listening port can be retrieved using {@link #getLocalPort()} after calling {@link #start()}.
     * The remote host and port may be set using {@link #setRemote(InetAddress, int)}, commands can then be sent there using {@link #sendCommand(AbstractCommand)}
     *
     * @param listener the listener that will be notified of new commands arriving at this CommandConnection
     */
    public CommandConnection(OnCommandListener listener) throws IOException {
        mCommandListener = listener;
    }

    /**
     * Retrieve the port this command connection is listening on
     */
    public int getLocalPort() {
        return mIncomingServer.getLocalPort();
    }

    /**
     * Assigns a new remote target for commands
     *
     * @param host host identification of the remote
     * @param port port the remote is listening on
     */
    public void setRemote(InetAddress host, int port) {
        mRemoteHost = host;
        mRemotePort = port;
    }

    /**
     * Assigns a new remote target for commands
     *
     * @param device remote device. must have address and commandport configured!
     */
    public void setRemote(NetworkDevice device) throws UnknownHostException {
        setRemote(device.getInetAddress(), device.commandPort);
    }

    /**
     * Use this function to send a command to the peer. It will assert that {@link #setRemote(InetAddress, int)} has been
     * used.
     *
     * @param command this command will be received by the peer
     * @throws IOException most probably if the socket is blocked TODO: definitive answer here
     */
    public void sendCommand(AbstractCommand command) throws IOException {
        // ensure that the remote host is properly configured
        // note: this is the common project. it does not recognize androids BuildConfig.DEBUG.
        assert (mRemotePort >= 0 && (mRemoteHost != null));

        // this is a try-with-resources; it will automatically close its resources when finished, whatever happens.
        try (Socket outboundSocket = new Socket(mRemoteHost, mRemotePort);
             ObjectOutputStream oos = new ObjectOutputStream(outboundSocket.getOutputStream())) {
            oos.writeObject(command);
        }
    }

    public boolean isRunningAndConfigured() {
        return mIncomingServer.isRunning() && mRemoteHost != null && mRemotePort >= 0;
    }

    /**
     * Use this to gracefully stop operations
     */
    public void close() {
        if (mIncomingServer != null)
            mIncomingServer.shutdown();
        mIncomingServer = null;
    }

    /**
     * Use this to start listening for commands. Does not throw if called multiple times.
     */
    public void start() throws IOException {
        if (mIncomingServer == null) {
            mIncomingServer = new CommandServer(this);
            mIncomingServer.start();
        }
    }

    /**
     * Called whenever our CommandServer receives a command packet.
     *
     * @param origin  the host which sent the command
     * @param command the command we received
     */
    // this could have been implemented by CommandConnection implementing the OnCommandListener, but because it is an
    // interface, it would have polluted our public surface with a method that is destined to be used by inner class
    // workings only.
    private void onCommand(InetAddress origin, AbstractCommand command) {
        mCommandListener.onCommand(origin, command);
    }


    /**
     * The {@link CommandServer} class is used to listen to incoming commands.
     */
    private static class CommandServer extends Thread {
        /**
         * This listener is called whenever a new command object is received.
         */
        CommandConnection mListener;

        /**
         * true if the server should continue running
         */
        private boolean mKeepRunning = true;

        /**
         * true while the server has not reached end of thread execution
         */
        private boolean mIsRunning = false;

        /**
         * Socket we use to listen
         */
        private ServerSocket mSocket;

        /**
         * A new CommandServer instance created will open a socket on a random free port to grant listening for commands.
         * TODO: what if the instance is created, but never started? the socket will stay open...
         *
         * @param listener this class will be called when a new command object is received
         * @throws IOException when an error occurs during binding of the socket
         */
        CommandServer(CommandConnection listener) throws IOException {
            // store listener
            mListener = listener;

            // get a socket now so we already know which port we will listen on
            mSocket = new ServerSocket(0);
        }

        /**
         * Returns the port the command server is listening on
         */
        int getLocalPort() {
            return mSocket.getLocalPort();
        }

        /**
         * Gracefully stop the server asap
         */
        void shutdown() {
            // kill the running server
            mKeepRunning = false;
        }

        /**
         * Listen on socket;
         * TODO: exception listener to handle exceptions in the thread more intelligently than "hopefully someone sees this just died"
         */
        public void run() {
            // continue running till shutdown() is called
            while (mKeepRunning) {
                mIsRunning = true;

                // try-with-resource: close the socket after accepting the connection
                try (Socket connection = mSocket.accept()) {
                    // create an object input stream from the new connection
                    ObjectInputStream oinput = new ObjectInputStream(connection.getInputStream());

                    // call listener with new command
                    mListener.onCommand(connection.getInetAddress(), (AbstractCommand) oinput.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // try to close the socket after finishing running
            try {
                if (mSocket != null)
                    mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mIsRunning = false;
        }

        public boolean isRunning() {
            return mIsRunning;
        }
    }
}
