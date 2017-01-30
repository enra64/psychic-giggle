package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Created by Ulrich on 29.11.2016.
 * <p>
 * This class receives the sensorData and normalizes it to an usable value. The source and target ranges
 * may be set using one of the various constructors, or {@link #setSourceRange(float)} and {@link #setTargetRange(float)}.
 * A custom sensitivity may be set; it will replace the default value of 1. The filter ignores its third
 * {@link #onData(NetworkDevice, SensorData, float)} parameter in any case.
 */
public class ScalingFilter extends AbstractFilter {
    /**
     * The range (maximum value, and inverted minimum value) the data should be projected to
     */
    private float mTargetRange;

    /**
     * The range (maximum value and inverted minimum value) the data lies in
     */
    private float mSourceRange;

    /**
     * A filter that normalizes the sensorData into usable input
     * and uses standard axes and a base sensitivity of 40f
     *
     * @param dataSink either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *                 must be called prior to starting operations.
     */
    public ScalingFilter(@Nullable NetworkDataSink dataSink) {
        this(100f, 100f, dataSink);
    }

    /**
     * A filter that normalizes the sensorData into usable input
     *
     * @param sink              either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *                          must be called prior to starting operations.
     * @param sourceRange       range of the data coming into the normalization filter
     * @param targetRange       range the data should be projected to
     */
    public ScalingFilter(float targetRange, float sourceRange, @Nullable NetworkDataSink sink) {
        super(sink);

        mTargetRange = targetRange;
        mSourceRange = sourceRange;
    }

    /**
     * Normalizes the sensorData
     *
     * @param sensorData the data received by the sensor
     */
    private void normalize(float[] sensorData) {
        float projectionFactor = mTargetRange / mSourceRange;

        for(int i = 0; i < sensorData.length; i++) {
            //Check if user generated values outside of set source range
            if(sensorData[i] < -mSourceRange)
                sensorData[i] = -mSourceRange;

            else if(sensorData[i] > mSourceRange)
                sensorData[i] = mSourceRange;

            sensorData[i] *= projectionFactor;
        }
    }

    /**
     * Change the range within which the source data lies
     *
     * @param range float maximum. the minimum will be negative range.
     */
    public void setSourceRange(float range) {
        mSourceRange = range;
    }

    /**
     * Change the resulting target range, as in the projection target range
     * @param targetRange the magnitude of the minimum and maximum values
     */
    public void setTargetRange(float targetRange) {
        mTargetRange = targetRange;
    }

    /**
     * Called when the next element should be filtered. Replaces the userSensitivity parameter with the set custom
     * sensitivity, or the default of 1.
     *
     * @param sensorData sensor data to process
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        normalize(sensorData.data);
        mDataSink.onData(origin, sensorData, userSensitivity);
    }




}
