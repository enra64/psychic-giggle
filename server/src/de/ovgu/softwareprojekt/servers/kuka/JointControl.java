package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.callback_interfaces.ResetListener;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.ButtonListener;

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
    private final static int PITCH_AXIS = 1;

    /**
     * The joint that is currently controlled by movement
     */
    private LbrJoint mCurrentJoint = LbrJoint.BaseRotator;

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
        float currentMaximum = KukaUtil.getRange(mCurrentJoint);

        //float rollValue = clampToJointRange(data.data[ROLL_AXIS] * userSensitivity * .03f, mRobotRollMaximum);
        float pitchValue = KukaUtil.clampToJointRange(data.data[PITCH_AXIS] * userSensitivity * .08f, currentMaximum);

        //System.out.println("Roll target: " + rollValue + ", pitch target: " + pitchValue);

        //mControlInterface.rotateJointTarget(mRobotRollJoint, rollValue);
        mControlInterface.rotateJointTarget(mCurrentJoint, pitchValue);
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
        if (click.isHold) {
            // change controlled joint
            mCurrentJoint = LbrJoint.values()[click.mID - 1];
            System.out.println("Using " + mCurrentJoint.name());
        }
    }

    /**
     * Called when the user presses the reset button on the app
     * @param origin which device pressed the button. may be null!
     */
    @Override
    public void onResetPosition(NetworkDevice origin) {
        mControlInterface.rotateJointTarget(mCurrentJoint, 0);
    }
}
