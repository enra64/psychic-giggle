package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 *
 */
public class MurmelControl implements NetworkDataSink {
    /**
     * The two axes we need for the murmel game
     */
    private final static int PITCH_AXIS = 1, ROLL_AXIS = 0;

    private final static LbrJoint
            mRobotPitchJoint = LbrJoint.ToolTilter,
            mRobotRollJoint = LbrJoint.ThirdRotator;

    private static final float
            mRobotPitchMaximum = LbrJointsRanges.getRange(mRobotPitchJoint) / 2,
            mRobotRollMaximum = LbrJointsRanges.getRange(mRobotRollJoint) / 2;

    private final LbrIIIIIIIIwa mControlInterface;

    public MurmelControl(LbrIIIIIIIIwa control) {
        mControlInterface = control;
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

        System.out.println("Roll target: " + rollValue + ", pitch target: " + pitchValue);

        mControlInterface.rotateJointTarget(mRobotRollJoint, rollValue);
        mControlInterface.rotateJointTarget(mRobotPitchJoint, pitchValue);
    }

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
}
