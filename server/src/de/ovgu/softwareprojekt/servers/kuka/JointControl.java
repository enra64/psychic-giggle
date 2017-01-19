package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.ButtonListener;

import static de.ovgu.softwareprojekt.servers.kuka.LbrJoint.*;

/**
 * Created by arne on 18.01.17.
 */
public class JointControl implements NetworkDataSink, ButtonListener, ResetListener {
    private final LbrIIIIIIIIwa mControlInterface;

    /**
     * The two axes we need for the murmel game
     */
    private final static int PITCH_AXIS = 1, ROLL_AXIS = 0;

    private LbrJoint mCurrentJoint = LbrJoint.BaseRotator;

    public JointControl(LbrIIIIIIIIwa robotInterface) {
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
        float currentMaximum = LbrJointRanges.getRange(mCurrentJoint);

        //float rollValue = capToRange(data.data[ROLL_AXIS] * userSensitivity * .03f, mRobotRollMaximum);
        float pitchValue = capToRange(data.data[PITCH_AXIS] * userSensitivity * .08f, currentMaximum);

        //System.out.println("Roll target: " + rollValue + ", pitch target: " + pitchValue);

        //mControlInterface.rotateJointTarget(mRobotRollJoint, rollValue);
        mControlInterface.rotateJointTarget(mCurrentJoint, pitchValue);
    }

    public void reset() {
        mControlInterface.rotateJointTarget(mCurrentJoint, 0);
    }

    /**
     * This is a somewhat custom capping function. It uses the negative of maxDisplacement as minimum.
     *
     * @param value           the value that should be capped
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

    /**
     * Called whenever a button is clicked
     *
     * @param click  event object specifying details like button id
     * @param origin the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick click, NetworkDevice origin) {
        // ignore release events
        if (click.isHold) {
            // change controlled joint
            mCurrentJoint = LbrJoint.values()[click.mID - 1];
            System.out.println("Using " + mCurrentJoint.name());
        }
    }

    @Override
    public void onResetPosition(NetworkDevice origin) {
        mControlInterface.rotateJointTarget(BaseRotator, 0);
        mControlInterface.rotateJointTarget(SecondRotator, 0);
        mControlInterface.rotateJointTarget(ThirdRotator, 0);
        mControlInterface.rotateJointTarget(ToolRotator, 0);
        mControlInterface.rotateJointTarget(BaseTilter, 0);
        mControlInterface.rotateJointTarget(SecondTilter, 0);
        mControlInterface.rotateJointTarget(ToolTilter, 0);
    }
}
