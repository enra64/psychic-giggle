package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * This filter zeroes sensor data when the amplitude of a specified axis does not exceed a set minimum.
 */
public class ThresholdingFilter extends AbstractFilter {
    /**
     * If this amplitude is not exceeded on {@link #mAxis}, the sensor data will be zeroed.
     */
    private final float mMinimumAmplitude;

    /**
     * The axis on which the amplitude is checked
     */
    private final int mAxis;

    /**
     * Create a new minimum amplitude filter.
     *
     * @param dataSink         either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *                         must be called prior to starting operations.
     * @param minimumAmplitude the minimum amplitude the values on <code>axis</code> must have for the data not to be zeroed
     * @param axis             the axis which must reach <code>minimumAmplitude</code>, {@literal x->0, y->1, z->2}
     */
    public ThresholdingFilter(@Nullable NetworkDataSink dataSink, float minimumAmplitude, int axis) {
        super(dataSink);
        mMinimumAmplitude = minimumAmplitude;
        mAxis = axis;
    }

    /**
     * Called when the next element should be filtered
     *
     * @param sensorData      sensor data to process
     * @param origin          which network device sent the data
     * @param userSensitivity which sensitivity was set by the user
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        // if the change does not meet the set minimum, zero the data
        if (abs(sensorData.data[mAxis]) < mMinimumAmplitude)
            Arrays.fill(sensorData.data, 0);
        // notify attached sink of new data
        forwardData(origin, sensorData, userSensitivity);
    }
}
