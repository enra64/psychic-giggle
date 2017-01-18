package de.ovgu.softwareprojekt.servers.kuka;

import coppelia.IntW;
import coppelia.remoteApi;

import java.util.EnumMap;

import static coppelia.remoteApi.simx_opmode_blocking;
import static coppelia.remoteApi.simx_opmode_oneshot;
import static coppelia.remoteApi.simx_return_ok;

/**
 * An implementation of the {@link LbrIIIIIIIIwa} interface for controlling a lbr iiwa robot in vrep
 */
public class Vrep implements LbrIIIIIIIIwa {
    private String mAddress;
    private int mPort;
    private remoteApi mVrep;
    private int mClientId;

    private EnumMap<LbrJoint, Integer> mJointMap = new EnumMap<>(LbrJoint.class);

    public Vrep(String ip, int port) {
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
            mJointMap.put(LbrJoint.values()[i - 1], getSimulationObject("LBR_iiwa_7_R800_joint" + i));

        // start the simulation in blocking mode to wait until it started
        mVrep.simxStartSimulation(mClientId, simx_opmode_blocking);
    }

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

    @Override
    public void rotateJoint(LbrJoint joint, float degree) {
        // because this is oneshot mode, the return code is basically useless
        // parameters are in radian
        int returnCode = mVrep.simxSetJointPosition(mClientId, mJointMap.get(joint), (float) (degree * Math.PI / 180), simx_opmode_oneshot);

        // check success
        //if(returnCode != simx_return_ok) throw new RoboticFailure("Could not move " + joint + "! Tried using " + mAddress + ":" + mPort);
    }

    @Override
    public void rotateJointTarget(LbrJoint joint, float degree) {
        // because this is oneshot mode, the return code is basically useless
        // parameters are in radian
        int returnCode = mVrep.simxSetJointTargetPosition(mClientId, mJointMap.get(joint), (float) (degree * Math.PI / 180), simx_opmode_oneshot);

        // check success
        //if(returnCode != simx_return_ok) throw new RoboticFailure("Could not move " + joint + "! Tried using " + mAddress + ":" + mPort);
    }

    @Override
    public void setJointVelocity(LbrJoint joint, float velocity) {
        // because this is oneshot mode, the return code is basically useless
        int returnCode = mVrep.simxSetJointTargetVelocity(mClientId, mJointMap.get(joint), velocity, simx_opmode_oneshot);

        // check success
        //if(returnCode != simx_return_ok) throw new RoboticFailure("Could not move " + joint + "! Tried using " + mAddress + ":" + mPort);
    }

    @Override
    public void stop() {
        mVrep.simxStopSimulation(mClientId, simx_opmode_blocking);
        mVrep.simxFinish(mClientId);
    }
}
