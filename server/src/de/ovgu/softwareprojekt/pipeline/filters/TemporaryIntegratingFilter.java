package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.Arrays;

/**
 * This filter sums up the previous n values
 */
public class TemporaryIntegratingFilter extends AbstractFilter {
    /**
     * Theses are storage variables, where the sum of al previous values is stored
     */
    private float[][] mValueStorage;

    /**
     * where should the next value be stored
     */
    private int mIndex = 0;

    /**
     * Create a new {@link TemporaryIntegratingFilter}
     *
     * @param sink   either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *               must be called prior to starting operations.
     * @param length the length after which sensor data will be ignored
     */
    public TemporaryIntegratingFilter(@Nullable NetworkDataSink sink, int length) {
        super(sink);
        mValueStorage = new float[length][3];
    }

    /**
     * Add current Values to the AXES_SUM and save them in the pipeline
     *
     * @param data current values
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        // store new data
        System.arraycopy(data.data, 0, mValueStorage[mIndex++], 0, data.data.length);

        // wraparound
        if (mIndex >= mValueStorage.length)
            mIndex = 0;

        // write over data with sum
        sum(data.data);

        mDataSink.onData(origin, data, userSensitivity);
    }

    /**
     * Sum all values (for each element of the triple) and store that in the parameter array
     *
     * @param target the destination for the values
     */
    private void sum(float[] target) {
        Arrays.fill(target, 0);

        for (float[] valueSet : mValueStorage)
            for (int i = 0; i < target.length; i++)
                target[i] += valueSet[i];
    }

    /**
     * This method sets all axis sums back to 0 in order to set a new initial position for the smart phone
     */
    public void resetFilter() {
        for (int i = 0; i < mValueStorage.length; i++)
            for (int j = 0; j < mValueStorage[i].length; j++)
                mValueStorage[i][j] = 0;
    }

}
