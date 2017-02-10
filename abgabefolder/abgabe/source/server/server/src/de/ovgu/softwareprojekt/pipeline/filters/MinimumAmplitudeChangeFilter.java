package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;


import static java.lang.Math.abs;

/**
 * This filter ignores changes in sensor data up to a certain margin to avoid shaking when trying not to move,
 * for example when pointing with a mouse
 */
public class MinimumAmplitudeChangeFilter extends AbstractFilter {
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
     * @param dataSink      either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *                      must be called prior to starting operations.
     * @param minimumChange the minimum of change a new data element must represent to be let through
     */
    public MinimumAmplitudeChangeFilter(@Nullable NetworkDataSink dataSink, float minimumChange) {
        super(dataSink);
        mMinimumChange = minimumChange;
    }

    /**
     * Called when the next element should be filtered
     *
     * @param sensorData      sensor data to process
     * @param origin          the network device that sent the data
     * @param userSensitivity the sensitivity set by the user
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        // initialise the storage vector on first data because we do not know its length beforehand
        if (mLastValue == null)
            mLastValue = new float[sensorData.data.length];

        if (calculateChange(sensorData.data, mLastValue) < mMinimumChange)
            // if the change does not meet the set minimum, overwrite the new data with our old, unchanged data
            System.arraycopy(mLastValue, 0, sensorData.data, 0, mLastValue.length);
        else
            // if the change does meet the minimum, copy over the new values
            System.arraycopy(sensorData.data, 0, mLastValue, 0, mLastValue.length);

        // notify attached sink of new data
        forwardData(origin, sensorData, userSensitivity);
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
