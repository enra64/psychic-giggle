package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.ConnectionAliveCheck;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class handles checking the connection age. It schedules itself. <b>You may not schedule
 * this class by yourself, call {@link #start()}.</b>
 * <p>It can be used in active mode with the constructor
 * {@link ConnectionWatch#ConnectionWatch(NetworkDevice, TimeoutListener, CommandConnection, ExceptionListener)},
 * where connection checks will be sent and {@link #onCheckEvent()} must be called when the remote
 * responds.</p>
 * <p>It can also be used in passive mode with the constructor
 * {@link ConnectionWatch#ConnectionWatch(NetworkDevice, TimeoutListener, ExceptionListener)}.
 * No network requests will be sent, but {@link #onCheckEvent()} must be called when a connection
 * check is requested and has been answered.</p>
 */
public class ConnectionWatch extends TimerTask {
    /**
     * Set this variable true to print the round trip times to System.out
     */
    private static final boolean CONNECTION_TIMES_LOG = false;
    /**
     * This ConnectionWatch watches the connection to this remote
     */
    private NetworkDevice mRemote;

    /**
     * Callback interface for timeouts
     */
    public interface TimeoutListener {
        /**
         * Called when a timeout was triggered.
         *
         * @param responseDelay delay between sending the check request and the answer
         */
        void onTimeout(long responseDelay);
    }

    /**
     * True if the timer is currently scheduled
     */
    private boolean mIsStarted;

    /**
     * This timer is used for scheduling the "still-alive" requests to our client
     */
    private Timer mConnectionCheckTimer = new Timer();

    /**
     * If the time between connection check requests exceeds this time, the connection is deemed dead.
     */
    private static final long MAXIMUM_CLIENT_RESPONSE_DELAY = 10000;// please do not increase this value without talking to me -Arne

    /**
     * This is the first of two timestamps used to keep check of the delay between sending the request
     * for a connection check and receiving the answer for it. It keeps track of the last time we sent
     * a connection check request
     */
    private volatile long mLastRequestTimestamp;

    /**
     * This is the second of the two timestamps for checking the delay between request and answer. It keeps
     * track of the last client response time.
     */
    private volatile long mLastCheckEventTimestamp;

    /**
     * Callback for connection timeouts
     */
    private final TimeoutListener mTimeoutListener;

    /**
     * Network device identifying ourselves
     */
    private final NetworkDevice mSelf;

    /**
     * The command connection used for sending the connection check requests
     */
    private final CommandConnection mOutConnection;

    /**
     * Callback for exceptions we could not gracefully handle
     */
    private final ExceptionListener mExceptionListener;

    /**
     * True if we are in active mode (eg sending requests)
     */
    private final boolean mIsActiveMode;

    /**
     * Create a new <b>active</b> ConnectionWatch. After {@link #start()} was called, {@link TimeoutListener#onTimeout(long)}
     * may be called if a timeout is detected. Call {@link #onCheckEvent()} whenever you receive
     * a connection check response.
     *
     * @param timeoutListener   callback for timeout notifications
     * @param exceptionListener callback for exceptions
     * @param self              network device identifying the local network entity
     * @param outConnection     this connection will be used to request connection checks
     */
    @SuppressWarnings("WeakerAccess")// must be public for common
    public ConnectionWatch(
            NetworkDevice self,
            TimeoutListener timeoutListener,
            CommandConnection outConnection,
            ExceptionListener exceptionListener) {
        mTimeoutListener = timeoutListener;
        mSelf = self;
        mOutConnection = outConnection;
        mExceptionListener = exceptionListener;

        // we are in active mode if we have an out connection
        mIsActiveMode = outConnection != null;

        // set informative thread name
        Thread.currentThread().setName("ConnectionWatch for " + mSelf);
    }

    /**
     * Create a new <b>passive</b> ConnectionWatch. After {@link #start()} was called, {@link TimeoutListener#onTimeout(long)}
     * may be called if a timeout is detected. No network requests will be sent. Call {@link #onCheckEvent()}
     * whenever you receive a connection check.
     *
     * @param timeoutListener   callback for timeout notifications
     * @param exceptionListener callback for exceptions
     * @param self              network device identifying the local network entity
     */
    public ConnectionWatch(
            NetworkDevice self,
            TimeoutListener timeoutListener,
            ExceptionListener exceptionListener) {
        this(self, timeoutListener, null, exceptionListener);
    }

    /**
     * Start operations. This will begin sending connection check requests after a short delay.
     */
    public void start() {
        // avoid immediate timeout through a zero (eg last response was 1970) here
        mLastCheckEventTimestamp = System.currentTimeMillis() + 900;
        mLastRequestTimestamp = mLastCheckEventTimestamp;
        // in ~one second, begin requesting a "still-alive" beep from the clients once per second
        mConnectionCheckTimer.scheduleAtFixedRate(this, 900, 500);

        mIsStarted = true;
    }

    /**
     * Set the remote this connectionwatch should watch
     *
     * @param remote the target of the connection this watch watches
     */
    public void setRemote(NetworkDevice remote) {
        mRemote = remote;

        // set informative thread name
        Thread.currentThread().setName("ConnectionWatch for " + mSelf);
    }

    /**
     * This runs a single iteration of the active mode:<br>
     * Check whether the client answered within {@link #MAXIMUM_CLIENT_RESPONSE_DELAY}ms
     * <ul>
     * <li>if it did, send next {@link ConnectionAliveCheck}</li>
     * <li>if not, notify {@link #mTimeoutListener}</li>
     * </ul>
     */
    private void activeModeIteration() {
        try {
            // check whether the maximum delay has been breached
            if ((mLastCheckEventTimestamp - mLastRequestTimestamp) > MAXIMUM_CLIENT_RESPONSE_DELAY)
                mTimeoutListener.onTimeout(mLastCheckEventTimestamp - mLastRequestTimestamp);
            // connection still alive
            else if (mOutConnection.isRunningAndConfigured()) {
                // send next connection check
                mOutConnection.sendCommand(new ConnectionAliveCheck(mSelf));

                // possibly print round trip time
                if(CONNECTION_TIMES_LOG)
                    System.out.println(mLastRequestTimestamp + "," + (mLastCheckEventTimestamp - mLastRequestTimestamp));

                // update the request timestamp
                mLastRequestTimestamp = System.currentTimeMillis();
            }
        } catch (ConnectException e) {
            // as this exception is thrown when the remote does not answer, a timeout is signalled
            // instead of calling the exception listener
            mTimeoutListener.onTimeout(mLastCheckEventTimestamp - mLastRequestTimestamp);
        } catch (IOException e) {
            // notify exception listener of exception
            mExceptionListener.onException(ConnectionWatch.this, e, "CONNECTION_WATCH_CHECK_FAILED: Could not check connection; probably offline.");
        }
    }

    /**
     * This runs the passive mode check, that is it checks whether the last check event occurred less
     * than {@link #MAXIMUM_CLIENT_RESPONSE_DELAY} ms in the past.
     */
    private void passiveModeCheck() {
        long delay = System.currentTimeMillis() - mLastCheckEventTimestamp;

        // true if the system has timed out
        if (delay > MAXIMUM_CLIENT_RESPONSE_DELAY) {
            // notify listener and user
            mTimeoutListener.onTimeout(mLastCheckEventTimestamp - mLastRequestTimestamp);
        }
    }

    /**
     * This is periodically called since we scheduled ourselves as a {@link TimerTask} in {@link #start()}
     */
    @Override
    public void run() {
        if (mIsActiveMode)
            activeModeIteration();
        else
            passiveModeCheck();
    }

    /**
     * Stops operations, no more requests will be sent after this, and no more checks will be executed.
     * <b>Does not close its command connection!</b>
     */
    public void close() {
        mConnectionCheckTimer.cancel();
    }

    /**
     * This function is used to notify the watch of a new interaction with the communication partner.
     */
    public void onCheckEvent() {
        mLastCheckEventTimestamp = System.currentTimeMillis();
    }

    /**
     * Get the running state of this connection watch
     *
     * @return true if the watch is running (eg its timer has been scheduled)
     */
    public boolean started() {
        return mIsStarted;
    }
}