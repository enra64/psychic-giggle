package de.ovgu.softwareprojekt.servers.nes;

/**
 * An exception for catching buttons in the button property file that could not be mapped to javas {@literal VK_<key>} codes.
 */
class InvalidButtonException extends Exception {
    /**
     * Constructs a new InvalidButtonException with the specified detail message. The cause is not initialized, and may subsequently
     * be initialized by a call to {@link Throwable#initCause(java.lang.Throwable)}.
     *
     * @param msg the detail message. The detail message is saved for later retrieval by the
     *            {@link Throwable#getMessage()} method.
     */
    InvalidButtonException(String msg) {
        super(msg);
    }
}
