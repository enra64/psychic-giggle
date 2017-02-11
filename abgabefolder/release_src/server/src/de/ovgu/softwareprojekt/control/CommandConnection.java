package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.AbstractCommand;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
 * first. Then you can use {@link #sendCommand(AbstractCommand)} to send any {@link AbstractCommand} subclass.
 * </p>
 */
@SuppressWarnings("WeakerAccess")
public class CommandConnection {
    /**
     * This listener must be notified of new commands arriving
     */
    private OnCommandListener mCommandListener;

    /**
     * The CommandListener is responsible for listening to incoming commands
     */
    private CommandListener mIncomingServer;

    /**
     * This host should receive the outbound commands
     */
    private InetAddress mRemoteHost = null;

    /**
     * This is the discoveryPort where outbound commands should arrive at {@link #mRemoteHost}
     */
    private int mRemotePort = -1;

    /**
     * The listener that will be notified of exceptions
     */
    private ExceptionListener mExceptionListener;

    /**
     * New control connection. The local listening port can be retrieved using {@link #getLocalPort()} after calling {@link #start()}.
     * The remote host and port may be set using {@link #setRemote(InetAddress, int)}, commands can then be sent there using {@link #sendCommand(AbstractCommand)}
     *
     * @param listener the listener that will be notified of new commands arriving at this CommandConnection
     */
    public CommandConnection(OnCommandListener listener, ExceptionListener exceptionListener) throws IOException {
        mCommandListener = listener;
        mExceptionListener = exceptionListener;
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
     * @param device remote device. Must have address and commandPort configured!
     * @throws UnknownHostException if the address was invalid
     */
    public void setRemote(NetworkDevice device) throws UnknownHostException {
        setRemote(device.getInetAddress(), device.commandPort);
    }

    /**
     * Use this function to send a command to the peer. It will assert that {@link #setRemote(InetAddress, int)} has been
     * used.
     *
     * @param command this command will be received by the peer
     * @throws IOException some I/O error. A ConnectException with ECONNREFUSED and ETIMEDOUT may also be thrown.
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

    /**
     * Check whether the connection is running and configured
     *
     * @return true if the connection is listening and configured with a remote
     */
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
     * @throws IOException if the port for command listening could not be bound
     */
    public void start() throws IOException {
        if (mIncomingServer == null) {
            mIncomingServer = new CommandListener(this);
            mIncomingServer.start();

            mIncomingServer.setName("CommandListener for " + mRemoteHost + ":" + mRemotePort);
        }
    }

    /**
     * Called whenever our CommandListener receives a command packet.
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
     * Immediately change the port to which commands will be sent
     *
     * @param remotePort the new command port
     */
    public void setRemotePort(int remotePort) {
        mRemotePort = remotePort;
    }


    /**
     * The {@link CommandListener} class is used to listen to incoming commands.
     */
    private class CommandListener extends Thread {
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
         * A new CommandListener instance created will open a socket on a random free port to grant listening for commands.
         *
         * @param listener this class will be called when a new command object is received
         * @throws IOException when an error occurs during binding of the socket
         */
        CommandListener(CommandConnection listener) throws IOException {
            super("CommandListener without remote");

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

            // try to close the socket
            if (mSocket != null)
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // ignored, because we cannot handle it anyway
                }
        }

        /**
         * Listen on socket, forward commands
         */
        public void run() {
            // continue running till shutdown() is called
            mIsRunning = true;

            while (mKeepRunning) {
                // try-with-resource: close the socket after accepting the connection
                try (Socket connection = mSocket.accept()) {
                    // create an object input stream from the new connection
                    ObjectInputStream oinput = new ObjectInputStream(connection.getInputStream());

                    // call listener with new command
                    mListener.onCommand(connection.getInetAddress(), (AbstractCommand) oinput.readObject());
                } catch (SocketException ignored) {
                    // this exception is thrown if #close() is called before #start(), but it is not relevant.
                } catch (ClassNotFoundException | IOException e) {
                    CommandConnection.this.mExceptionListener.onException(CommandConnection.this, e, "Could not listen for commands");
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

        /**
         * Returns true if we are currently in the listening loop
         *
         * @return true if we are currently in the listening loop
         */
        public boolean isRunning() {
            return mIsRunning;
        }
    }
}
