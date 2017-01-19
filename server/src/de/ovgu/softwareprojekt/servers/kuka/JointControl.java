package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.callback_interfaces.ButtonListener;

/**
 * The joint control class controls a single joint using pitch data
 */
public class JointControl implements NetworkDataSink, ButtonListener, ResetListener {
    /**
     * The robot control interface used for communications
     */
    private final LbrIiiiiiiwa mControlInterface;

    /**
     * The mobile phone axes
     */
    private final static int PITCH_AXIS = 1, ROLL_AXIS = 0;

    /**
     * The joint that is currently controlled by movement
     */
    private LbrJoint
            mCurrentRotator = LbrJoint.BaseRotator,
            mCurrentTilter = LbrJoint.BaseTilter;

    /**
     * Button ids for selecting different joints. Only needed for joint control mode
     * <p>
     * NOTE: if you change theses ids, you're gonna have a bad time. see {@link #onButtonClick(ButtonClick, NetworkDevice)}
     */
    static final int BASE_PAIR_BUTTON = 1, CENTER_PAIR_BUTTON = 2, TOOL_PAIR_BUTTON = 3;

    /**
     * Create a new JointControl instance
     *
     * @param robotInterface the control interface to be used for movement etc.
     */
    JointControl(LbrIiiiiiiwa robotInterface) {
        mControlInterface = robotInterface;
    }

    /**
     * onData is called whenever new data is to be processed
     *
     * @param origin          the network device which sent the data
     * @param data            the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        float tilterMaxRange = KukaUtil.getRange(mCurrentTilter);
        float rotatorMaxRange = KukaUtil.getRange(mCurrentRotator);

        //float rollValue = clampToJointRange(data.data[ROLL_AXIS] * userSensitivity * .03f, mRobotRollMaximum);
        float tilterValue = KukaUtil.clampToJointRange(data.data[PITCH_AXIS] * userSensitivity * .08f, tilterMaxRange);
        float rotatorValue = KukaUtil.clampToJointRange(data.data[ROLL_AXIS] * userSensitivity * .08f, rotatorMaxRange);

        mControlInterface.rotateJointTarget(mCurrentTilter, tilterValue);
        mControlInterface.rotateJointTarget(mCurrentRotator, rotatorValue);
    }

    /**
     * called when the sink is no longer used and should no longer require resources
     */
    @Override
    public void close() {
    }

    /**
     * Called whenever a button is clicked
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        // ignore button release events
        if (!click.isHold) return;

        // WARNING: because of the KukaServer#onButtonClick implementation, robot state must not be changed in onButtonClick!

        // update the currently used actor pair
        switch (click.mID) {
            case BASE_PAIR_BUTTON:
                mCurrentRotator = LbrJoint.BaseRotator;
                mCurrentTilter = LbrJoint.BaseTilter;
                break;
            case CENTER_PAIR_BUTTON:
                mCurrentRotator = LbrJoint.SecondRotator;
                mCurrentTilter = LbrJoint.SecondTilter;
                break;
            case TOOL_PAIR_BUTTON:
                mCurrentRotator = LbrJoint.ToolArmRotator;
                mCurrentTilter = LbrJoint.ToolTilter;
                break;
        }
    }

    /**
     * Called when the user presses the reset button on the app
     *
     * @param origin which device pressed the button. may be null!
     */
    @Override
    public void onResetPosition(NetworkDevice origin) {
        mControlInterface.rotateJointTarget(mCurrentRotator, 0);
        mControlInterface.rotateJointTarget(mCurrentTilter, 0);
    }
}
