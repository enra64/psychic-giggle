package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.commands.ConnectionAliveCheck;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class handles checking the connection age
 */
class ConnectionWatch {
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
     * This timer is used for scheduling the "still-alive" requests to our client
     */
    private Timer mConnectionCheckTimer = new Timer();

    /**
     * If the time between connection check requests exceeds this time, the connection is deemed dead.
     */
    private static final long MAXIMUM_CLIENT_RESPONSE_DELAY = 1000;

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
    private volatile long mLastClientResponse;

    /**
     * Callback for connection timeouts
     */
    private TimeoutListener mTimeoutListener;

    /**
     * Network device identifying ourselves
     */
    private NetworkDevice mSelf;

    /**
     * The command connection used for sending the connection check requests
     */
    private CommandConnection mOutConnection;

    /**
     * Callback for exceptions we could not gracefully handle
     */
    private ExceptionListener mExceptionListener;

    /**
     * Create a new ConnectionWatch. After {@link #start()} was called, {@link TimeoutListener#onTimeout(long)}
     * may be called if a timeout is detected
     *
     * @param timeoutListener callback for timeout notifications
     */
    ConnectionWatch(NetworkDevice self, TimeoutListener timeoutListener, CommandConnection outConnection, ExceptionListener exceptionListener) {
        mTimeoutListener = timeoutListener;
        mSelf = self;
        mOutConnection = outConnection;
        mExceptionListener = exceptionListener;
    }

    /**
     * Start operations. This will begin sending connection check requests after a short delay.
     */
    void start() {
        // avoid immediate timeout through a zero (eg last response was 1970) here
        mLastClientResponse = System.currentTimeMillis() + 900;
        mLastRequestTimestamp = mLastClientResponse;

        // in one second, begin requesting a "still-alive" beep from the clients once per second
        mConnectionCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // check whether the maximum delay has been breached
                    if ((mLastClientResponse - mLastRequestTimestamp) > MAXIMUM_CLIENT_RESPONSE_DELAY) {
                        mTimeoutListener.onTimeout(mLastClientResponse - mLastRequestTimestamp);
                    }
                    // send a new alive check
                    else if (mOutConnection.isRunningAndConfigured()) {
                        mOutConnection.sendCommand(new ConnectionAliveCheck(mSelf));

                        // update the timestamp
                        mLastRequestTimestamp = System.currentTimeMillis();
                    }
                } catch (IOException e) {
                    mExceptionListener.onException(ConnectionWatch.this, e, "CONNECTION_WATCH_CHECK_FAILED: Could not check connection; probably offline.");
                }
            }
        }, 900, 500);
    }

    /**
     * Stops operations, no more requests will be sent after this, and no more checks will be executed.
     */
    void close() {
        mConnectionCheckTimer.cancel();
    }

    /**
     * This function is used to notify the watch of a new response. If you forget to call this, a timeout
     * will be detected rather soon.
     */
    void clientResponded() {
        mLastClientResponse = System.currentTimeMillis();
    }
}