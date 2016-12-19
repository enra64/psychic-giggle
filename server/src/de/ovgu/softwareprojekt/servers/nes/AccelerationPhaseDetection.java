package de.ovgu.softwareprojekt.servers.nes;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Created by arne on 12/19/16.
 */
public class AccelerationPhaseDetection implements NetworkDataSink {
    public interface AccelerationListener {
        void onUpMovement();
        void onDownMovement();
    }

    private AccelerationListener mListener;

    private static final int TIMEOUT = 10;

    public AccelerationPhaseDetection(AccelerationListener listener){
        mListener = listener;
    }

    private boolean
            mDownCycle = false,
            mDownPhase2 = false,
            mDownPhase3 = false;

    private int mTimeoutCounter = 0;

    private boolean
            mUpCycle = false,
            mUpPhase2 = false,
            mUpPhase3 = false;

    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData) {
        onData(sensorData.data[2] > 500, sensorData.data[2] < -500);
    }

    private void onData(boolean up, boolean down) {
        System.out.println(toString());
        if (!mUpCycle) {
            if (mDownCycle) {
                if ((mDownPhase3 && !up && !down) || mTimeoutCounter++ > TIMEOUT) {
                    mDownCycle = false;
                    mDownPhase2 = false;
                    mDownPhase3 = false;
                    mTimeoutCounter = 0;
                }
                if (mDownPhase2 && down && !up)
                    mDownPhase3 = true;
                if (up && !down)
                    mDownPhase2 = true;
            }
            if (!mDownCycle && down && !up) {
                mDownCycle = true;
                mListener.onDownMovement();
            }
        }

        if(!mDownCycle){
            if(mUpCycle){
                if((mUpPhase3 && !down && !up) || mTimeoutCounter++ > TIMEOUT){
                    mUpCycle = false;
                    mUpPhase2 = false;
                    mUpPhase3 = false;
                    mTimeoutCounter = 0;
                }
                if(mUpPhase2 && up && !down)
                    mUpPhase3 = true;
                if(down && !up)
                    mUpPhase2 = true;
            }
            if(!mUpCycle && up && !down){
                mUpCycle = true;
                mListener.onUpMovement();
            }
        }
    }

    @Override
    public String toString() {
        return "AccelerationPhaseDetection{" +
                "mDownCycle=" + mDownCycle +
                ", mDownPhase2=" + mDownPhase2 +
                ", mDownPhase3=" + mDownPhase3 +
                ", mUpCycle=" + mUpCycle +
                ", mUpPhase2=" + mUpPhase2 +
                ", mUpPhase3=" + mUpPhase3 +
                '}';
    }

    @Override
    public void close() {

    }
}
