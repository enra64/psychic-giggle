package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * This filter zeroes sensor data when the difference between two successive data points is not large enough
 */
public class DifferenceThresholdFilter extends AbstractFilter {
    /**
     * If the sum of changes does not at least equal this value, the sensor data will be zeroed
     */
    private final float mMinimumChange;

    /**
     * Last value that was accepted as "changed enough"
     */
    private float[] mLastValue = null;

    /**
     * Create a new minimum amplitude filter.
     *
     * @param dataSink      either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *                      must be called prior to starting operations.
     * @param minimumChange the minimum of change a new data element must represent to be let through
     */
    public DifferenceThresholdFilter(@Nullable NetworkDataSink dataSink, float minimumChange) {
        super(dataSink);
        mMinimumChange = minimumChange;
    }

    /**
     * Called when the next element should be filtered
     *
     * @param sensorData      sensor data to process
     * @param origin          which network device sent the data
     * @param userSensitivity sensitivity set by the user
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        // initialise the storage vector on first data because we do not know its length beforehand
        if (mLastValue == null)
            mLastValue = new float[sensorData.data.length];

        // if the change does not meet the set minimum, overwrite the new data with our old, unchanged data
        if (calculateChange(sensorData.data, mLastValue) < mMinimumChange)
            Arrays.fill(sensorData.data, 0);
            // if the change does meet the minimum, copy over the new values
        else {
            //System.out.println(calculateChange(sensorData.data, mLastValue));
            System.arraycopy(sensorData.data, 0, mLastValue, 0, mLastValue.length);
        }
        // notify attached sink of new data
        mDataSink.onData(origin, sensorData, userSensitivity);
    }

    /**
     * Calculate the sum of the absolute changes
     *
     * @param dataA data vector number one
     * @param dataB data vector number two
     * @return sum of absolute differences
     */
    private float calculateChange(float[] dataA, float[] dataB) {
        // both value vectors must have the same length
        assert dataA.length == dataB.length;

        // calculate and return the sum of differences between both vectors
        float change = 0;

        // we are checking for both A and B length. that is a dirty hack circumventing the problems we seem to have
        for (int i = 0; i < dataA.length && i < dataB.length; i++)
            change += abs(dataB[i] - dataA[i]);
        return change;
    }

}
