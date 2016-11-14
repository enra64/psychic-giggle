package de.ovgu.softwareprojekt.misc;

import java.io.IOException;

/**
 * This interface should be implemented by classes who want to be notified when exceptions occur in parallel classes
 */
public interface OnIoExceptionListener {
    /**
     * Called when an IOException has occurred
     * @param culprit the object that reports the exception.
     * @param exception the exception that was thrown
     */
    void onIoException(Object culprit, IOException exception);
}
