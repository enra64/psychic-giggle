package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This class scales sensor values. It maps the sensor data from [<code>-sourceRange</code>, <code>sourceRange</code>] to
 * [<code>-targetRange</code>, <code>targetRange</code>]. It is useful for converting sensor values into value ranges that
 * are more easily usable.
 * <p>
 * Values exceeding the source range will be mapped to the corresponding extremum of the target range.
 * <p>
 * The source and target ranges may be set using one of the various constructors, or {@link #setSourceRange(float)}
 * and {@link #setTargetRange(float)}.
 * <p>
 * If the source and target range have not been manually set, no scaling will be executed.
 */
public class ScalingFilter extends AbstractFilter {
    /**
     * The [<code>-targetRange</code>, <code>targetRange</code>] the data should be projected to
     */
    private float mTargetRange;

    /**
     * The [<code>-sourceRange</code>, <code>sourceRange</code>] the data should be mapped from
     */
    private float mSourceRange;

    /**
     * Create a new {@link ScalingFilter}
     *
     * @param dataSink either a valid network data sink, or null. <p>If null, a {@link NetworkDataSink} must be set before
     *                 using the filter, see {@link #setDataSink(NetworkDataSink)}.
     */
    public ScalingFilter(@Nullable NetworkDataSink dataSink) {
        // map from max_value to max_value, so that no data will can be capped
        this(Float.MAX_VALUE, Float.MAX_VALUE, dataSink);
    }

    /**
     * Create a new {@link ScalingFilter}.
     * <p>
     * A {@link NetworkDataSink} must be set before using the filter, see {@link #setDataSink(NetworkDataSink)}, as is
     * done automatically be {@link de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder}
     */
    public ScalingFilter() {
        this(null);
    }

    /**
     * Create a new {@link ScalingFilter}.
     * <p>
     * A {@link NetworkDataSink} must be set before using the filter, see {@link #setDataSink(NetworkDataSink)}, as is
     * done automatically be {@link de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder}
     *
     * @param sourceRange The [<code>-sourceRange</code>, <code>sourceRange</code>] the data should be mapped from
     * @param targetRange The [<code>-targetRange</code>, <code>targetRange</code>] the data should be projected to
     */
    public ScalingFilter(float targetRange, float sourceRange) {
        this(targetRange, sourceRange, null);
    }

    /**
     * A filter that normalizes the sensorData into usable input
     *
     * @param sink        either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *                    must be called prior to starting operations.
     * @param sourceRange The [<code>-sourceRange</code>, <code>sourceRange</code>] the data should be mapped from
     * @param targetRange The [<code>-targetRange</code>, <code>targetRange</code>] the data should be projected to
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

        for (int i = 0; i < sensorData.length; i++) {
            //Check if user generated values outside of set source range
            if (sensorData[i] < -mSourceRange)
                sensorData[i] = -mSourceRange;

            else if (sensorData[i] > mSourceRange)
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
     *
     * @param targetRange the magnitude of the minimum and maximum values
     */
    public void setTargetRange(float targetRange) {
        mTargetRange = targetRange;
    }

    /**
     * Called when the next element should be filtered.
     *
     * @param origin          the network device which sent the data
     * @param sensorData      the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings for the sensor
     *                        the data is from
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        normalize(sensorData.data);
        forwardData(origin, sensorData, userSensitivity);
    }


}
