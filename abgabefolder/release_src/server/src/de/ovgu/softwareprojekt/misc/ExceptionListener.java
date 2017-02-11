package de.ovgu.softwareprojekt.misc;

/**
 * Interface for exception identification across threads
 */
public interface ExceptionListener {
    /**
     * Called when an exception cannot be gracefully handled.
     *
     * @param origin    the instance (or, if it is a hidden instance, the known parent) that produced the exception
     * @param exception the exception that was thrown
     * @param info additional information to help identify the problem
     */
    void onException(Object origin, Exception exception, String info);
}
