package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.security.InvalidParameterException;
import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * This filter zeroes sensor data when the amplitude of an acis does not exceed a set minimum.
 */
public class ThresholdingFilter extends AbstractFilter {
    /**
     * If the sum of changes does not at least equal this value, the last value that changed will
     * be sent as an update instead.
     */
    private final float mMinimumAmplitude;

    /**
     * The axis for which our filter should be the safeguard
     */
    private final int mAxis;

    /**
     * Create a new minimum amplitude filter.
     *
     * @param dataSink         either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *                         must be called prior to starting operations.
     * @param minimumAmplitude the minimum of  a new data element must represent to be let through
     * @param axis             the axis which must have a min amplitude, x->0, y->1, z->2
     */
    public ThresholdingFilter(@Nullable NetworkDataSink dataSink, float minimumAmplitude, int axis) {
        super(dataSink, 0, 1, 2);
        mMinimumAmplitude = minimumAmplitude;

        switch (axis) {
            case 0:
                mAxis = XAXIS;
                break;
            case 1:
                mAxis = YAXIS;
                break;
            case 2:
                mAxis = ZAXIS;
                break;
            default:
                throw new InvalidParameterException("Unknown axis parameter");
        }
    }

    /**
     * Called when the next element should be filtered
     *
     * @param sensorData sensor data to process
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData) {
        // if the change does not meet the set minimum, zero the data
        if (abs(sensorData.data[mAxis]) < mMinimumAmplitude)
            Arrays.fill(sensorData.data, 0);
        // notify attached sink of new data
        mDataSink.onData(origin, sensorData);
    }
}
