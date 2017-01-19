package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import static de.ovgu.softwareprojekt.servers.kuka.LbrJoint.*;

/**
 *
 */
public class MurmelControl implements NetworkDataSink, ResetListener {
    /**
     * The two axes we need for the murmel game
     */
    private final static int PITCH_AXIS = 1, ROLL_AXIS = 0;

    private final static LbrJoint
            mRobotPitchJoint = LbrJoint.ToolTilter,
            mRobotRollJoint = LbrJoint.ThirdRotator;

    private static final float
            mRobotPitchMaximum = LbrJointRanges.getRange(mRobotPitchJoint) / 2,
            mRobotRollMaximum = LbrJointRanges.getRange(mRobotRollJoint) / 2;

    private final LbrIIIIIIIIwa mControlInterface;

    public MurmelControl(LbrIIIIIIIIwa control) {
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
        float rollValue = capToRange(data.data[ROLL_AXIS] * userSensitivity * .03f, mRobotRollMaximum);
        float pitchValue = capToRange(data.data[PITCH_AXIS] * userSensitivity * .03f, mRobotPitchMaximum);

        //System.out.println("Roll target: " + rollValue + ", pitch target: " + pitchValue);

        mControlInterface.rotateJointTarget(mRobotRollJoint, rollValue);
        mControlInterface.rotateJointTarget(mRobotPitchJoint, pitchValue);
    }

    /**
     * This is a somewhat custom capping function. It uses the negative of maxDisplacement as minimum.
     * @param value the value that should be capped
     * @param maxDisplacement the maximum and negative minimum the value should be after this
     * @return the capped value
     */
    private float capToRange(float value, float maxDisplacement) {
        if (value > maxDisplacement)
            return maxDisplacement;
        if (value < -maxDisplacement)
            return maxDisplacement;
        return value;
    }

    /**
     * called when the sink is no longer used and should no longer require resources
     */
    @Override
    public void close() {

    }

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
