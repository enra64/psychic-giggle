package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.callback_interfaces.ButtonListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import static de.ovgu.softwareprojekt.servers.kuka.Joint.*;

/**
 * This class may be used for controlling the robot in a marble-labyrinth friendly way. Only the two topmost joints are
 * moved.
 */
public class MarbleLabyrinthControl implements NetworkDataSink, ResetListener, ButtonListener {
    /**
     * Axis we need for the marble game
     */
    private final static int PITCH_AXIS = 1, ROLL_AXIS = 0;

    /**
     * The joints we use for expressing the pitch and roll angles
     */
    private final static Joint
            mRobotPitchJoint = Joint.ToolTilter,
            mRobotRollJoint = Joint.ToolArmRotator;

    /**
     * The maxima of our pitch and roll joints
     */
    private static final float
            mRobotPitchMaximum = KukaUtil.getRange(mRobotPitchJoint) / 2,
            mRobotRollMaximum = KukaUtil.getRange(mRobotRollJoint) / 2;

    /**
     * Button ids for swapping the movement direction for tilting/rotating action
     * <p>
     * NOTE: if you change theses ids, you're gonna have a bad time. see {@link #onButtonClick(ButtonClick, NetworkDevice)}
     */
    static final int FLIP_ROTATOR_BUTTON = 1, FLIP_TILTER_BUTTON = 2;

    /**
     * True if the direction of control should be flipped.
     */
    private boolean mRotatorIsFlipped = false, mTilterIsFlipped = false;

    /**
     * The control interface for the robot
     */
    private final LbrIiiiiiiwa mControlInterface;

    /**
     * Create a new marble labyrinth control class.
     *
     * @param control the control interface for the robot
     */
    MarbleLabyrinthControl(LbrIiiiiiiwa control) {
        mControlInterface = control;
        onResetPosition(null);
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
        float rollValue = KukaUtil.clampToJointRange(data.data[ROLL_AXIS] * userSensitivity * .03f, mRobotRollMaximum);
        float pitchValue = KukaUtil.clampToJointRange(data.data[PITCH_AXIS] * userSensitivity * .03f, mRobotPitchMaximum);

        if(mRotatorIsFlipped)
            rollValue *= -1;
        if(mTilterIsFlipped)
            pitchValue *= -1;

        //System.out.println("Roll target: " + rollValue + ", pitch target: " + pitchValue);

        mControlInterface.rotateJointTarget(mRobotRollJoint, rollValue);
        mControlInterface.rotateJointTarget(mRobotPitchJoint, pitchValue);
    }

    /**
     * called when the sink is no longer used and should no longer require resources
     */
    @Override
    public void close() {

    }

    /**
     * Reset robot to a position that should make the marble labyrinth game easily playable
     *
     * @param origin the device that sent the reset command
     */
    @Override
    public void onResetPosition(NetworkDevice origin) {
        mControlInterface.rotateJointTarget(BaseRotator, 0);
        mControlInterface.rotateJointTarget(SecondRotator, 0);
        mControlInterface.rotateJointTarget(ToolArmRotator, 0);
        mControlInterface.rotateJointTarget(ToolRotator, 10);
        mControlInterface.rotateJointTarget(BaseTilter, 45);
        mControlInterface.rotateJointTarget(SecondTilter, -45);
        mControlInterface.rotateJointTarget(ToolTilter, 0);
    }

    /**
     * Called whenever a button is clicked
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        // ignore release events
        if(!click.isHold)
            return;

        // WARNING: because of the KukaServer#onButtonClick implementation, robot state must not be changed in onButtonClick!

        switch (click.mID) {
            case FLIP_ROTATOR_BUTTON:
                mRotatorIsFlipped = !mRotatorIsFlipped;
                break;
            case FLIP_TILTER_BUTTON:
                mTilterIsFlipped = !mTilterIsFlipped;
                break;
        }
    }
}
