package de.ovgu.softwareprojektapp.activities.send;

/**
 * This exception is thrown when the layout file sent by the server could not be parsed
 */
public class InvalidLayoutException extends Exception {
    /**
     * This exception is thrown when the layout file sent by the server could not be parsed
     * @param msg reason for parsing failure
     */
    public InvalidLayoutException(String msg){
        super(msg);
    }
}
