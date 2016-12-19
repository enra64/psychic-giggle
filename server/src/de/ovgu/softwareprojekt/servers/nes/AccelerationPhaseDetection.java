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

    public AccelerationPhaseDetection(AccelerationListener listener){
        mListener = listener;
    }

    private boolean
            mDownCycle = false,
            mDownPhase2 = false,
            mDownPhase3 = false;

    private boolean
            mUpCycle = false,
            mUpPhase2 = false,
            mUpPhase3 = false;

    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData) {
        onData(sensorData.data[2] > 1000, sensorData.data[2] < -1000);
    }

    private void onData(boolean up, boolean down) {
        if (!mUpCycle) {
            if (mDownCycle) {
                if (mDownPhase3 && !up && !down) {
                    mDownCycle = false;
                    mDownPhase2 = false;
                    mDownPhase3 = false;
                }
                if (mDownPhase2 && down && !up)
                    mDownPhase3 = true;
                if (up && !down)
                    mDownPhase2 = true;
            }
            if (down && !up) {
                mDownCycle = true;
                mListener.onDownMovement();
            }
        }

        if(!mDownCycle){
            if(mUpCycle){
                if(mUpPhase3 && !down && !up){
                    mUpCycle = false;
                    mUpPhase2 = false;
                    mUpPhase3 = false;
                }
                if(mUpPhase2 && up && !down)
                    mUpPhase3 = true;
                if(down && !up)
                    mUpPhase2 = true;
            }
            if(up && !down){
                mUpCycle = true;
                mListener.onUpMovement();
            }
        }
    }

    @Override
    public void close() {

    }
}
