package de.ovgu.softwareprojekt.discovery;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * This abstract class serves as a skeleton for implementing both the discovery server implementation as well as the discovery
 * client implementation.
 */
@SuppressWarnings("WeakerAccess")
public abstract class DiscoveryThread extends Thread {
    /**
     * The socket this thread should use for all of its network operations
     */
    private DatagramSocket mSocket;

    /**
     * True if the thread should keep running
     */
    private boolean mIsRunning = false;

    /**
     * True if the thread was started once
     */
    private boolean mHasRun = false;

    /**
     * A NetworkDevice that identifies the device this Object is running on
     */
    private NetworkDevice mSelfDeviceId;

    /**
     * Create a new DiscoveryThread when you already know your command- and data port, for example for use in a server
     * announcing where to connect.
     *
     * @param selfName         the discovery thread will announce this name to other devices seeking partners in the network
     * @param localCommandPort a command server must be listening on this port to receive further communication
     * @param localDataPort    a data server should be listening here to receive further communication
     */
    public DiscoveryThread(String selfName, int localCommandPort, int localDataPort) {
        mSelfDeviceId = new NetworkDevice(selfName, localCommandPort, localDataPort);
    }

    /**
     * Create a new DiscoveryThread. Only use this constructor if you do not yet know your command- and data port (!),
     * most probably when you want to implement a client, as a server not responding with a valid command port is useless.
     *
     * @param selfName the discovery thread will announce this name to other devices seeking partners in the network
     */
    public DiscoveryThread(String selfName) {
        mSelfDeviceId = new NetworkDevice(selfName);
    }

    /**
     * This function must be overridden to complete the DiscoveryThread. You probably want to do something like
     * <pre>
     *     {@code
     *     while(isRunning()){
     *         listen();
     *     }
     *     }
     * </pre>
     */
    @Override
    public abstract void run();

    /**
     * Send an object identifying the configured network device.
     *
     * @param targetHost to which target the message should be sent
     * @param targetPort at which discoveryPort the message should arrive on targetHost
     * @throws IOException if the identification message could not be sent
     */
    public void sendSelfIdentification(InetAddress targetHost, int targetPort) throws IOException {
        // create a new object output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);

        // write the object into a byte buffer
        os.writeObject(mSelfDeviceId);
        byte[] data = outputStream.toByteArray();

        // create an UDP packet from the byte buffer and send it to the desired host/discoveryPort combination
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, targetHost, targetPort);
        mSocket.send(sendPacket);
    }

    /**
     * Start the thread. To quote the java api specification "It is never legal to start a thread
     * more than once. In particular, a thread may not be restarted once it has completed execution."
     * Use {@link #hasRun()} to check whether you may start the thread
     */
    @Override
    public synchronized void start() {
        mHasRun = true;
        mIsRunning = true;
        super.start();
    }

    /**
     * Check whether this threads {@link #start()} has already been called. If it returns true,
     * start() may not be called on this instance!
     */
    public boolean hasRun(){
        return mHasRun;
    }

    /**
     * Wait for a NetworkDevice object to arrive on the set {@link DatagramSocket}. Other messages will be discarded.
     *
     * @throws IOException either a {@link SocketTimeoutException} if the wait times out, or any other IO exception that occurse
     */
    protected void listen() throws IOException {
        // wait for a message on our socket
        byte[] recvBuf = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
        mSocket.receive(receivePacket);

        // initialise an ObjectInputStream
        ByteArrayInputStream input = new ByteArrayInputStream(recvBuf);
        ObjectInputStream oinput = new ObjectInputStream(input);

        NetworkDevice identification;
        try {
            // create the NetworkDevice describing our remote partner
            identification = (NetworkDevice) oinput.readObject();
            identification.address = receivePacket.getAddress().getHostAddress();

            // notify sub-class
            onDiscovery(identification);

            // if the cast to NetworkDevice fails, this message was not sent by the discovery system, and may be ignored
        } catch (ClassNotFoundException ignored) {
        }
    }

    protected abstract void onDiscovery(NetworkDevice newDevice);

    /**
     * Check whether the server is set to stay running
     *
     * @return true if the server will continue running
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    /**
     * Socket currently configured to be used by the discovery system
     *
     * @return the socket
     */
    protected DatagramSocket getSocket() {
        return mSocket;
    }

    /**
     * Set the socket to be used by this {@link DiscoveryThread}
     *
     * @param socket new socket configuration
     * @throws SocketException if some of the internal configuration failed
     */
    protected void setSocket(DatagramSocket socket) throws SocketException {
        // socket must be broadcasting
        socket.setBroadcast(true);

        // save socket
        mSocket = socket;

        // update our local discoveryPort; this is the only definitive source
        mSelfDeviceId.discoveryPort = socket.getLocalPort();
    }

    public void setPorts(int commandPort, int dataPort){
        mSelfDeviceId.commandPort = commandPort;
        mSelfDeviceId.dataPort = dataPort;
    }

    /**
     * Signal the running while loop to stop
     */
    public void close() {
        mIsRunning = false;
    }
}
