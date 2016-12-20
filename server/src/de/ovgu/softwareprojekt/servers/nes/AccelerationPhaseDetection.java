package de.ovgu.softwareprojekt.servers.nes;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This class may be used to detect sudden movements in the z-axis of linear acceleration of a device.
 * The movement is expected to return to the original position.
 */
public class AccelerationPhaseDetection implements NetworkDataSink {
    /**
     * This interface must be implemented if you want to be notified of the detected movements
     */
    public interface AccelerationListener {
        /**
         * Called when an up-movement has been detected
         */
        void onUpMovement();

        /**
         * Called when a down-movement has been detected
         */
        void onDownMovement();
    }

    /**
     * Create a new acceleration phase detection class.
     * @param listener this listener will be notified when movements have been identified
     */
    AccelerationPhaseDetection(AccelerationListener listener) {
        mListener = listener;
    }

    /**
     * This counter is used for aborting phase waits, avoiding bad recognition of the next
     * event due to bad phase state.
     */
    private int mTimeoutCounter = 0;

    /**
     * This is the maximum event count the detector will wait until the current cycle is aborted.
     */
    private static final int TIMEOUT = 10;

    /**
     * State variables determining the current state of an up movement. True if the phase is currently live.
     */
    private boolean
            mUpCycle = false,
            mUpPhase2 = false,
            mUpPhase3 = false;

    /**
     * State variables determining the current state of an up movement. True if the phase is currently live.
     */
    private boolean
            mDownCycle = false,
            mDownPhase2 = false,
            mDownPhase3 = false;

    /**
     * The listener we call back when a cycle begins
     */
    private AccelerationListener mListener;

    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData) {
        int threshold = getThreshold();
        onData(sensorData.data[2] > threshold, sensorData.data[2] < -threshold);
    }

    /**
     * Get the different threshold values for detecting phase switches
     * @return one of three different thresholds, depending on the expected amplitude
     */
    private int getThreshold(){
        if(mUpPhase3 || mDownPhase3)
            return 150;
        if(mUpPhase2 || mDownPhase2)
            return 1000;
        return 500;
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

        if (!mDownCycle) {
            if (mUpCycle) {
                if ((mUpPhase3 && !down && !up) || mTimeoutCounter++ > TIMEOUT) {
                    mUpCycle = false;
                    mUpPhase2 = false;
                    mUpPhase3 = false;
                    mTimeoutCounter = 0;
                }
                if (mUpPhase2 && up && !down)
                    mUpPhase3 = true;
                if (down && !up)
                    mUpPhase2 = true;
            }
            if (!mUpCycle && up && !down) {
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
