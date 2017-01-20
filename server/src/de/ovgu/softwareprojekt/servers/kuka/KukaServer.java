package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.AbstractServer;
import de.ovgu.softwareprojekt.pipeline.filters.AveragingFilter;
import de.ovgu.softwareprojekt.pipeline.splitters.Switch;

import java.io.IOException;

/**
 * This is the LBR iiwa control class. It supports two operation modes: joint control, where individual joints may be
 * controlled using the phone pitch, and marble labyrinth control, where only the topmost joints are moved, so a marble
 * labyrinth can be played
 */
public class KukaServer extends AbstractServer {
    /**
     * The control interface for the robot
     */
    private LbrIiiiiiiwa mRobotInterface;

    /**
     * If true, only the ToolTilter and the ToolArmRotator joints will be used, so as to enable playing a marble labyrinth
     */
    private boolean mIsInMarbleMode = true;

    /**
     * This is the button id for the "change control mode" button
     */
    private static final int MODE_BUTTON = 0;

    /**
     * This is a pipeline splitter that we use to switch between sending the joint control or the marble labyrinth control
     * all data.
     */
    private Switch mModeDataSwitch;

    /**
     * The joint control. Used for controlling individual joints
     */
    private JointControl mJointControl;

    /**
     * The marble labyrinth control
     */
    private MarbleLabyrinthControl mMarbleLabyrinthControl;

    /**
     * The control mode that should currently be notified of reset events
     */
    private ResetListener mCurrentControl;

    /**
     * Create a new {@link KukaServer}
     * @throws IOException if communication with a device fails
     */
    public KukaServer() throws IOException {
        super(null);

        mRobotInterface = new Vrep("127.0.0.1", 19997);
        mRobotInterface.start();

        createButtons();

        // create both control instances
        mMarbleLabyrinthControl = new MarbleLabyrinthControl(mRobotInterface);
        mCurrentControl = mMarbleLabyrinthControl;
        mJointControl = new JointControl(mRobotInterface);

        // create a new data switch
        mModeDataSwitch = new Switch(new AveragingFilter(3, mMarbleLabyrinthControl), mJointControl, true);

        registerDataSink(mModeDataSwitch, SensorType.Gravity);
        setSensorOutputRange(SensorType.Gravity, 100);
    }

    /**
     * Reset robot position. Appropriate target state is determined by current control class.
     *
     * @param origin the device that sent the data
     */
    @Override
    public void onResetPosition(NetworkDevice origin) {
        mCurrentControl.onResetPosition(origin);
    }

    /**
     * Called when exceptions are generated that could not be handled by the framework
     * @param o the object from where the exception originates
     * @param e the exception that was thrown
     * @param s additional info provided at the site of exception
     */
    @Override
    public void onException(Object o, Exception e, String s) {
        System.out.println("Exception in " + o.getClass() + ", info: " + s);
        System.out.flush();
        e.printStackTrace();
    }

    /**
     * Accept any new client
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
     * Helper function to create all necessary buttons
     * @throws IOException if the buttons could not be created
     */
    private void createButtons() throws IOException {
        clearButtons();
        addButton("Switch between marble labyrinth and joint control mode", MODE_BUTTON);

        // in marble mode, we want to be able to switch the movement directions
        if (mIsInMarbleMode) {
            addButton("Flip rotator direction", MarbleLabyrinthControl.FLIP_ROTATOR_BUTTON);
            addButton("Flip tilter direction", MarbleLabyrinthControl.FLIP_TILTER_BUTTON);
        // in joint control mode, three rotator/tilter pairs can be controlled
        } else {
            addButton("Control base actor pair", JointControl.BASE_PAIR_BUTTON);
            addButton("Control center actor pair", JointControl.CENTER_PAIR_BUTTON);
            addButton("Control tool actor pair", JointControl.TOOL_PAIR_BUTTON);
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
        // handle buttons
        switch (click.mID) {
            case MODE_BUTTON:
                // toggle mode
                mIsInMarbleMode = !mIsInMarbleMode;

                // update current control instance
                mCurrentControl = mIsInMarbleMode ? mMarbleLabyrinthControl : mJointControl;

                // reset the joints
                mCurrentControl.onResetPosition(null);

                // notify the pipeline switch of the new data destination
                mModeDataSwitch.routeToFirst(mIsInMarbleMode);

                // try to create the necessary buttons
                try {
                    createButtons();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Switched to " + (mIsInMarbleMode ? "marble mode" : "joint mode"));
                break;
            default:
                // forward all other buttons to both controls.
                // WARNING: this relies on neither class changing the robot state in a button event!
                mJointControl.onButtonClick(click, origin);
                mMarbleLabyrinthControl.onButtonClick(click, origin);
                break;
        }
    }
}
