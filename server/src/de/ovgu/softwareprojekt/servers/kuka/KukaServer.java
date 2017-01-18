package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.Server;
import de.ovgu.softwareprojekt.pipeline.filters.AveragingFilter;
import de.ovgu.softwareprojekt.pipeline.filters.IntegratingFilter;
import de.ovgu.softwareprojekt.pipeline.splitters.Switch;

import java.io.IOException;

import static de.ovgu.softwareprojekt.servers.kuka.LbrJoint.*;

/**
 * T
 */
public class KukaServer extends Server {
    private LbrIIIIIIIIwa mRobotInterface;
    private boolean mIsInMurmelMode = true;

    private static final int MODE_BUTTON = 0;
    private static final int
            JOINT1_BUTTON = 1,
            JOINT2_BUTTON = 2,
            JOINT3_BUTTON = 3,
            JOINT4_BUTTON = 4,
            JOINT5_BUTTON = 5,
            JOINT6_BUTTON = 6,
            JOINT7_BUTTON = 7;

    private Switch mModeDataSwitch;

    public KukaServer() throws IOException {
        super(null);

        mRobotInterface = new Vrep("127.0.0.1", 19997);
        mRobotInterface.start();

        initJoints();
        createButtons();

        // create a new data switch
        mModeDataSwitch = new Switch(new AveragingFilter(3, new MurmelControl(mRobotInterface)), new JointControl(), true);

        registerDataSink(mModeDataSwitch, SensorType.Gravity);
        setSensorOutputRange(SensorType.Gravity, 100);
    }

    private void initJoints() {
        mRobotInterface.rotateJointTarget(BaseRotator, 0);
        mRobotInterface.rotateJointTarget(SecondRotator, 0);
        mRobotInterface.rotateJointTarget(ThirdRotator, 0);
        mRobotInterface.rotateJointTarget(ToolRotator, 10);
        mRobotInterface.rotateJointTarget(BaseTilter, 45);
        mRobotInterface.rotateJointTarget(SecondTilter, -45);
        mRobotInterface.rotateJointTarget(ToolTilter, 0);
    }

    @Override
    public void onResetPosition(NetworkDevice origin) {
        initJoints();
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

    private void createButtons() throws IOException {
        clearButtons();
        addButton("Switch between murmel/general mode", MODE_BUTTON);

        // the joints can only be individually controlled when not in murmel mode
        if (!mIsInMurmelMode) {
            addButton("JOINT 1", JOINT1_BUTTON);
            addButton("JOINT 2", JOINT2_BUTTON);
            addButton("JOINT 3", JOINT3_BUTTON);
            addButton("JOINT 4", JOINT4_BUTTON);
            addButton("JOINT 5", JOINT5_BUTTON);
            addButton("JOINT 6", JOINT6_BUTTON);
            addButton("JOINT 7", JOINT7_BUTTON);
        }
    }

    /**
     * Called whenever a button is clicked
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        switch (click.mID) {
            case MODE_BUTTON:
                // toggle mode
                mIsInMurmelMode = !mIsInMurmelMode;

                // reset the joints
                initJoints();

                try {
                    createButtons();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
