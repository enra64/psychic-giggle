package de.ovgu.softwareprojekt.servers.kuka.vrep;

import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.Server;

import static de.ovgu.softwareprojekt.servers.kuka.LbrJoints.*;

/**
 * Created by arne on 18.01.17.
 */
public class VrepServer extends Server {
    private Vrep mVrep;

    public VrepServer(){
        super(null);

        System.out.println("java.library.path: " + System.getProperty("java.library.path"));
        System.out.flush();

        mVrep = new Vrep("127.0.0.1", 19997);
        mVrep.start();
        //mVrep.setJointVelocity(Joint2, 8000);
        mVrep.rotateJoint(Joint6, 40);
    }

    @Override
    public void onResetPosition(NetworkDevice origin) {
        System.out.println("ignored a reset call");
    }

    @Override
    public void onException(Object o, Exception e, String s) {
        System.out.println("Exception in " + o.getClass() + ", info: " + s);
        System.out.flush();
        e.printStackTrace();
    }

    /**
     * Check whether a new client should be accepted
     *
     * @param newClient the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    @Override
    public boolean acceptClient(NetworkDevice newClient) {
        return true;
    }

    /**
     * Called when a client sent a disconnect signal
     *
     * @param disconnectedClient the lost client
     */
    @Override
    public void onClientDisconnected(NetworkDevice disconnectedClient) {
        System.out.println(disconnectedClient + " disconnected");
    }

    /**
     * Called when a client hasn't responded to connection check requests within 500ms
     *
     * @param timeoutClient the client that did not respond
     */
    @Override
    public void onClientTimeout(NetworkDevice timeoutClient) {
        System.out.println(timeoutClient + " had a timeout");
    }

    /**
     * called when a Client is successfully connected
     *
     * @param connectedClient the client that connected successfully
     */
    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {
        System.out.println("Accepted client " + connectedClient);
    }

    /**
     * Called whenever a button is clicked
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {

    }
}
