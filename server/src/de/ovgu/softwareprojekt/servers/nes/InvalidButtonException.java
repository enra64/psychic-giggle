package de.ovgu.softwareprojekt.servers.nes;

/**
 * Created by arne on 1/2/17.
 */
public class InvalidButtonException extends Exception {
    InvalidButtonException(String msg){
        super(msg);
    }
}
