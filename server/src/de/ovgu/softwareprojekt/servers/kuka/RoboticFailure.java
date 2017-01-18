package de.ovgu.softwareprojekt.servers.kuka;

/**
 * Created by arne on 18.01.17.
 */
public class RoboticFailure extends RuntimeException {
    public RoboticFailure(String msg){
        super(msg);
    }
}
