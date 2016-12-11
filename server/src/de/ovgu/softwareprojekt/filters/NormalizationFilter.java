package de.ovgu.softwareprojekt.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;

/**
 * Created by Ulrich on 29.11.2016.
 * <p>
 * This class receives the sensorData and normalizes it to an usable value. The sensor data may be scaled using a custom
 * factor after normalization takes place, see {@link #setCustomSensitivity(float)}. The source and target ranges
 * may be set using one of the various constructors, or {@link #setSourceRange(float)} and {@link #setTargetRange(float)}
 */
public class NormalizationFilter extends AbstractFilter {
    /**
     * Allows the user to change the base value SENSITIVITY by adding or subtracting this value
     */
    private float mCustomSensitivity = 0f;

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
     * @param dataSink where to put filtered data
     */
    public NormalizationFilter(DataSink dataSink) {
        this(dataSink, 50f, 1000f, 1000f);
    }

    /**
     * A filter for normalizing incoming data
     *
     * @param sink              where to put filtered data
     * @param customSensitivity the custom sensitivity value. will be applied before normalization.
     * @param sourceRange       range of the data coming into the normalization filter
     * @param targetRange       range the data should be projected to
     */
    public NormalizationFilter(DataSink sink, float customSensitivity, float sourceRange, float targetRange) {
        this(sink, customSensitivity, sourceRange, targetRange, 0, 1, 2);
    }

    /**
     * A filter that normalizes the sensorData into usable input
     *
     * @param sink              where to put filtered data
     * @param customSensitivity the custom sensitivity value. will be applied before normalization.
     * @param sourceRange       range of the data coming into the normalization filter
     * @param targetRange       range the data should be projected to
     * @param xaxis             TODO schöne beschreibung von diesem parameter
     * @param yaxis
     * @param zaxis
     */
    public NormalizationFilter(DataSink sink, float customSensitivity, float sourceRange, float targetRange, int xaxis, int yaxis, int zaxis) {
        super(sink, xaxis, yaxis, zaxis);

        mCustomSensitivity = customSensitivity;
        mTargetRange = targetRange;
        mSourceRange = sourceRange;
    }

    /**
     * Normalizes the sensorData
     *
     * @param sensorData the data received by the sensor
     */
    private void normalize(float[] sensorData) {
        float projectionFactor = getProjectionFactor();
        sensorData[XAXIS] *= projectionFactor;
        sensorData[YAXIS] *= projectionFactor;
        sensorData[ZAXIS] *= projectionFactor;
    }

    public void setCustomSensitivity(float customValue) {
        mCustomSensitivity = customValue;
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
     * Get the factor that must be used to project from source to target range
     */
    private float getProjectionFactor() {
        return mCustomSensitivity * (mTargetRange / mSourceRange);
    }

    /**
     * Called when the next element should be filtered
     *
     * @param sensorData sensor data to process
     */
    @Override
    public void onData(SensorData sensorData) {
        normalize(sensorData.data);
        mDataSink.onData(sensorData);
    }

    public void setTargetRange(float targetRange) {
        mTargetRange = targetRange;
    }
}
