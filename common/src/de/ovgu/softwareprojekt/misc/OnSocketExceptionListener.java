package de.ovgu.softwareprojekt.misc;

import java.net.SocketException;

/**
 * This interface should be implemented by classes who want to be notified when exceptions occur in parallel classes
 */
public interface OnSocketExceptionListener {
    /**
     * Called when a SocketException has occurred
     * @param culprit the object that reports the exception.
     * @param exception the exception that was thrown
     */
    void onSocketException(Object culprit, SocketException exception);
}
