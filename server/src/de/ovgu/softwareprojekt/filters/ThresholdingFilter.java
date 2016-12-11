package de.ovgu.softwareprojekt.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;

import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * This filter zeros sensor data not exceeding a certain margin to avoid zittering when trying not to move,
 * for example when pointing with a mouse
 */
public class ThresholdingFilter extends AbstractFilter {
    /**
     * If the sum of changes does not at least equal this value, the last value that changed will
     * be sent as an update instead.
     */
    private final float mMinimumChange;

    /**
     * Last value that was accepted as "changed enough"
     */
    private float[] mLastValue = null;

    /**
     * Create a new minimum amplitude filter.
     *
     * @param dataSink      where to put filtered data
     * @param minimumChange the minimum of change a new data element must represent to be let through
     */
    public ThresholdingFilter(DataSink dataSink, float minimumChange) {
        super(dataSink, 0, 1, 2);
        mMinimumChange = minimumChange;
    }

    /**
     * Called when the next element should be filtered
     *
     * @param sensorData sensor data to process
     */
    @Override
    public void onData(SensorData sensorData) {
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
        mDataSink.onData(sensorData);
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
