package de.ovgu.softwareprojekt.misc;

/**
 *
 */
public interface ExceptionListener {
    void onException(Object origin, Exception exception, String info);
}
