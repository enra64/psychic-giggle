package de.ovgu.softwareprojekt.examples.kuka;

import coppelia.IntW;
import coppelia.remoteApi;

import java.util.EnumMap;

import static coppelia.remoteApi.simx_opmode_blocking;
import static coppelia.remoteApi.simx_opmode_oneshot;
import static coppelia.remoteApi.simx_return_ok;

/**
 * An implementation of the {@link LbrIiwa} interface for controlling a kuka lbr iiwa robot in V-REP
 */
public class Vrep implements LbrIiwa {
    /**
     * Address of the simulation environment
     */
    private String mAddress;

    /**
     * Port of the simulation environment
     */
    private int mPort;

    /**
     * The remoteApi instance we use for communication with V-REP
     */
    private remoteApi mVrep;

    /**
     * The client id V-REP gave us
     */
    private int mClientId;

    /**
     * A mapping from {@link Joint}s to their keys in V-REP
     */
    private EnumMap<Joint, Integer> mJointMap = new EnumMap<>(Joint.class);

    /**
     * Create a new V-REP control instance
     * @param ip Address of the simulation environment
     * @param port Port of the simulation environment
     */
    Vrep(String ip, int port) {
        mAddress = ip;
        mPort = port;
    }

    @Override
    public void start() {
        mVrep = new remoteApi();

        // close possibly previously created connections
        mVrep.simxFinish(-1);

        // connect to vrep
        mClientId = mVrep.simxStart(mAddress, mPort, true, true, 4000, 5);

        // check connection success
        if (mClientId != simx_return_ok)
            throw new RoboticFailure("Could not connect to VREP! Tried using " + mAddress + ":" + mPort);

        // get all joints
        for(int i = 1; i <= 7; i++)
            mJointMap.put(Joint.values()[i - 1], getSimulationObject("LBR_iiwa_7_R800_joint" + i));

        // start the simulation in blocking mode to wait until it started
        mVrep.simxStartSimulation(mClientId, simx_opmode_blocking);
    }

    /**
     * Retrieve a simulation object (for example a joint)
     * @param objectName name of the object in V-REP
     * @return the V-REP id of this object
     */
    private Integer getSimulationObject(String objectName) {
        // hello c style
        IntW objectHandle = new IntW(0);

        // try to get the object
        int returnCode = mVrep.simxGetObjectHandle(mClientId, objectName, objectHandle, simx_opmode_blocking);

        // check success
        if(returnCode != simx_return_ok)
            throw new RoboticFailure("Could not get joint! Tried using " + mAddress + ":" + mPort);

        // return handle
        return objectHandle.getValue();
    }

    /**
     * Rotate a joint.
     *
     * @param joint  which joint
     * @param degree how much.
     */
    @Override
    public void rotateJoint(Joint joint, float degree) {
        // because this is oneshot mode, the return code is basically useless
        // parameters are in radian
        mVrep.simxSetJointPosition(mClientId, mJointMap.get(joint), (float) (degree * Math.PI / 180), simx_opmode_oneshot);
    }

    /**
     * Rotate a joint to a position.
     *
     * @param joint  which joint
     * @param degree where to
     */
    @Override
    public void rotateJointTarget(Joint joint, float degree) {
        // because this is oneshot mode, the return code is basically useless
        // parameters are in radian
        mVrep.simxSetJointTargetPosition(mClientId, mJointMap.get(joint), (float) (degree * Math.PI / 180), simx_opmode_oneshot);
    }

    /**
     * Set the velocity of a joint
     * @param joint which joint
     * @param velocity how fast
     */
    @Override
    public void setJointVelocity(Joint joint, float velocity) {
        // because this is oneshot mode, the return code is basically useless
        mVrep.simxSetJointTargetVelocity(mClientId, mJointMap.get(joint), velocity, simx_opmode_oneshot);
    }

    /**
     * Finalize operations. The other {@link LbrIiwa} methods, except for start(), need not be callable after calling stop().
     */
    @Override
    public void stop() {
        mVrep.simxStopSimulation(mClientId, simx_opmode_blocking);
        mVrep.simxFinish(mClientId);
    }
}
