package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.Command;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class CommandConnection implements CommandSink, CommandSource {
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
     * The remote host and port may be set using {@link #setRemote(InetAddress, int)}
     */
    public CommandConnection() throws IOException {
    }

    /**
     * Retrieve the port this command connection is listening on
     */
    public int getLocalPort() {
        return mIncomingServer.getLocalPort();
    }

    /**
     * Configure the remote host
     *
     * @param host host identification of the remote
     * @param port port the remote is listening on
     */
    public void setRemote(InetAddress host, int port) {
        mRemoteHost = host;
        mRemotePort = port;
    }

    /**
     * Use this function to send a command to the peer. It will assert that {@link #setRemote(InetAddress, int)} has been
     * used.
     *
     * @param command this command will be received by the peer
     * @throws IOException most probably if the socket is blocked TODO: definitive answer here
     */
    @Override
    public void inputCommand(Command command) throws IOException {
        // ensure that the remote host is properly configured
        assert (mRemotePort >= 0 && mRemoteHost != null);

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
        if (mIncomingServer != null)
            mIncomingServer.shutdown();
        mIncomingServer = null;
    }

    /**
     * Set the listener that will be notified of new commands arriving at this CommandConnection
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
    public void start() throws IOException {
        if (mIncomingServer == null) {
            mIncomingServer = new CommandServer(this);
            mIncomingServer.start();
        }
    }

    /**
     * Called whenever our CommandServer receives a command packet.
     *
     * @param command the command we received
     */
    // this could have been implemented by CommandConnection implementing the OnCommandListener, but because it is an
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
        CommandConnection mListener;

        /**
         * true if the server should continue running
         */
        private boolean mRunning = true;

        /**
         * Socket we are listening on
         */
        private ServerSocket mSocket;

        CommandServer(CommandConnection listener) throws IOException {
            mListener = listener;

            // get a socket now so we know which port we are listening on
            mSocket = new ServerSocket(0);
        }

        int getLocalPort(){
            return mSocket.getLocalPort();
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

            try (Socket connection = mSocket.accept()) {

                // notify user of working state
                System.out.println("command connection on discoveryPort " + mSocket.getLocalPort());

                // get an object input stream; this is open as long as connection is open,
                // so the tcp connection stays open as long as Data is transmitted
                oinput = new ObjectInputStream(connection.getInputStream());

                // read objects on the same connection until shutdown() is called
                while (mRunning) {
                    // call listener with new object
                    mListener.onCommand((Command) oinput.readObject());
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            try {
                if (mSocket != null)
                    mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
