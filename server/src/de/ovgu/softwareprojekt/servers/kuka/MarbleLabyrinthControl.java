package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import static de.ovgu.softwareprojekt.servers.kuka.LbrJoint.*;

/**
 * This class may be used for controlling the robot in a marble-labyrinth friendly way. Only the two topmost joints are
 * moved.
 */
public class MarbleLabyrinthControl implements NetworkDataSink, ResetListener {
    /**
     * Axis we need for the marble game
     */
    private final static int PITCH_AXIS = 1, ROLL_AXIS = 0;

    /**
     * The joints we use for expressing the pitch and roll angles
     */
    private final static LbrJoint
            mRobotPitchJoint = LbrJoint.ToolTilter,
            mRobotRollJoint = LbrJoint.ThirdRotator;

    /**
     * The maxima of our pitch and roll joints
     */
    private static final float
            mRobotPitchMaximum = KukaUtil.getRange(mRobotPitchJoint) / 2,
            mRobotRollMaximum = KukaUtil.getRange(mRobotRollJoint) / 2;

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
        mControlInterface.rotateJointTarget(ThirdRotator, 0);
        mControlInterface.rotateJointTarget(ToolRotator, 10);
        mControlInterface.rotateJointTarget(BaseTilter, 45);
        mControlInterface.rotateJointTarget(SecondTilter, -45);
        mControlInterface.rotateJointTarget(ToolTilter, 0);
    }
}
