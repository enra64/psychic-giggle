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
     *
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
     * True if we are currently in an up movement cycle
     */
    private boolean mUpCycle = false;

    /**
     * True if we are currently in a down movement cycle
     */
    private boolean mDownCycle = false;

    /**
     * State variables determining the current state of an up movement. True if the phase is currently live.
     */
    private boolean mPhase2 = false, mPhase3 = false;

    /**
     * The listener we call back when a cycle begins
     */
    private AccelerationListener mListener;

    /**
     * @param networkDevice   where the data stems from
     * @param sensorData      the sensor data
     * @param userSensitivity currently ignored to simplify detection
     */
    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData, float userSensitivity) {
        float threshold = getThreshold() + 240 * (((100 - userSensitivity) - 50) / 100);
        onData(sensorData.data[2] > threshold, sensorData.data[2] < -threshold);
    }

    /**
     * Get the different threshold values for detecting phase switches
     *
     * @return one of three different thresholds, depending on the expected amplitude
     */
    private float getThreshold() {
        // the 3rd & last phase has the lowest amplitudes
        if (mPhase3) return 40f;
        // the second phase has the highest amplitudes
        if (mPhase2) return 160f;
        // the first phase has medium amplitudes
        return 160f;
    }

    /**
     * This function tries to detect whether the current conditions are sufficient to either enter a
     * cycle or advance the current movement phase
     *
     * @param up   true, if the current data exceeds the movement threshold
     * @param down true, if the current data exceeds the movement threshold
     */
    private void onData(boolean up, boolean down) {
        // only allow down cycle detection if no up cycle is live
        if (!mUpCycle) {
            if (mDownCycle) {
                // true if the cycle has ended, or the timeout has been met
                if ((mPhase3 && !up && !down) || mTimeoutCounter++ > TIMEOUT) {
                    mDownCycle = false;
                    mPhase2 = false;
                    mPhase3 = false;
                    mTimeoutCounter = 0;
                }

                // checks for phase 3 conditions (initial direction)
                if (mPhase2 && down && !up) {
                    mPhase3 = true;
                    mTimeoutCounter = 0;
                }

                // check for phase 2 conditions (reversed direction)
                if (up && !down) {
                    mPhase2 = true;
                    mTimeoutCounter = 0;
                }
            }

            // detect a down cycle start (initial direction)
            if (!mDownCycle && down && !up) {
                mDownCycle = true;
                mListener.onDownMovement();
            }
        }

        // only allow up cycle detection if no down cycle is live
        if (!mDownCycle) {
            if (mUpCycle) {
                // true if the cycle has ended, or the timeout has been met
                if ((mPhase3 && !down && !up) || mTimeoutCounter++ > TIMEOUT) {
                    mUpCycle = false;
                    mPhase2 = false;
                    mPhase3 = false;
                    mTimeoutCounter = 0;
                }

                // check for phase 3 conditions (initial direction)
                if (mPhase2 && up && !down) {
                    mPhase3 = true;
                    mTimeoutCounter = 0;
                }

                // check for phase 2 conditions (reversed direction)
                if (down && !up) {
                    mPhase2 = true;
                    mTimeoutCounter = 0;
                }
            }

            // detect an up cycle start (initial direction)
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
                ", mPhase2=" + mPhase2 +
                ", mPhase3=" + mPhase3 +
                ", mUpCycle=" + mUpCycle +
                '}';
    }

    /**
     * Does not need to do anything
     */
    @Override
    public void close() {
    }
}
