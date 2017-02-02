package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.callback_interfaces.ButtonListener;
import de.ovgu.softwareprojekt.callback_interfaces.ClientListener;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

/**
 * This subclass of AbstractPsychicServer can be used if you only want to know about specific callbacks, or cannot subclass
 * AbstractPsychicServer. The server will accept all clients, unless {@link #acceptClient(NetworkDevice)} is overridden or a
 * {@link ClientListener} is set using {@link #setClientListener(ClientListener)}.
 */
@SuppressWarnings("WeakerAccess")
public class PsychicServer extends AbstractPsychicServer {
    private ClientListener mClientListener;
    private ExceptionListener mExceptionListener;
    private ButtonListener mButtonListener;
    private ResetListener mResetListener;

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     *
     * @param serverName    if not null, this name will be used. otherwise, the devices hostname is used
     * @param discoveryPort the port that should be listened on to discover devices
     */
    public PsychicServer(@Nullable String serverName, int discoveryPort) {
        super(serverName, discoveryPort);
    }

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called. The
     * discovery port is defaulted to port 8888
     *
     * @param serverName the name this server will be discoverable as
     */
    public PsychicServer(String serverName) {
        super(serverName);
    }

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called.
     * The device hostname will be used as server name.
     *
     * @param discoveryPort the discovery port to use
     */
    public PsychicServer(int discoveryPort) {
        super(discoveryPort);
    }

    /**
     * Create a new server. It will be offline (not using any sockets) until {@link #start()} is called. Port 8888 will
     * be used as the discovery port, and the device hostname will be used as server name.
     */
    public PsychicServer() {
        super();
    }

    /**
     * Set the listener for client events. If functions of this interface are overridden in child classes, the client
     * listener is no longer called by them.
     *
     * @param clientListener the new client listener
     */
    public void setClientListener(@Nullable ClientListener clientListener) {
        mClientListener = clientListener;
    }

    /**
     * Set the listener for reset button events. If functions of this interface are overridden in child classes, the reset
     * listener is no longer called by them.
     *
     * @param resetListener the new client listener
     */
    public void setResetListener(@Nullable ResetListener resetListener) {
        mResetListener = resetListener;
    }

    /**
     * Set the listener for button events. If functions of this interface are overridden in child classes, the button
     * listener is no longer called by them.
     *
     * @param buttonListener the new client listener
     */
    public void setButtonListener(@Nullable ButtonListener buttonListener) {
        mButtonListener = buttonListener;
    }

    /**
     * Set the listener for exception events. If functions of this interface are overridden in child classes, the exception
     * listener is no longer called by them.
     *
     * @param exceptionListener the new client listener
     */
    public void setExceptionListener(@Nullable ExceptionListener exceptionListener) {
        mExceptionListener = exceptionListener;
    }

    /**
     * Called when the user presses the reset position button. If overridden, the reset listener (if set) will no longer
     * be called
     *
     * @param origin which device pressed the button
     */
    @Override
    public void onResetPosition(NetworkDevice origin) {
        if (mResetListener != null) mResetListener.onResetPosition(origin);
    }

    /**
     * Check whether a new client should be accepted
     *
     * @param newClient the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    @Override
    public boolean acceptClient(NetworkDevice newClient) {
        return mClientListener == null || mClientListener.acceptClient(newClient);
    }

    /**
     * Called when a client sent a disconnect signal
     *
     * @param disconnectedClient the lost client
     */
    @Override
    public void onClientDisconnected(NetworkDevice disconnectedClient) {
        if (mClientListener != null) mClientListener.onClientDisconnected(disconnectedClient);
    }

    /**
     * Called when a client hasn't responded to connection check requests within 500ms
     *
     * @param timeoutClient the client that did not respond
     */
    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {
        if (mClientListener != null) mClientListener.onClientTimeout(timeoutClient);
    }

    /**
     * called when a Client is successfully connected
     *
     * @param connectedClient the client that connected successfully
     */
    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {
        if (mClientListener != null) mClientListener.onClientAccepted(connectedClient);
    }

    /**
     * Called whenever a button is clicked
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        if (mButtonListener != null) mButtonListener.onButtonClick(click, origin);
    }

    /**
     * Called when an exception cannot be gracefully handled.
     *
     * @param origin    the instance (or, if it is a hidden instance, the known parent) that produced the exception
     * @param exception the exception that was thrown
     * @param info      additional information to help identify the problem
     */
    @Override
    public void onException(Object origin, Exception exception, String info) {
        if(mExceptionListener != null) mExceptionListener.onException(origin, exception, info);
    }
}
