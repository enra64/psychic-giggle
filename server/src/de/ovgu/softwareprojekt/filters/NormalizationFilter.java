package de.ovgu.softwareprojekt.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;

/**
 * Created by Ulrich on 29.11.2016.
 *
 * This class receives the sensorData and normalizes (?) it to a usable value by multiplying it
 * with a base sensitivity value which can be changed by subtracting or adding a custom value to
 * the sensitivity
 */
public class NormalizationFilter extends AbstractFilter {
    /**
     * Allows the user to change the base value SENSITIVITY by adding or subtracting this value
     */
    private float mCustomSensitivity = 0f;

    /**
     * A filter that normalizes the sensorData into usable input
     * and uses standard axes and a base sensitivity of 40f
     * @param dataSink      where to put filtered data
     */
    public NormalizationFilter(DataSink dataSink){
        this(dataSink, 0f, 0, 1, 2);
    }

    /**
     * standard constructor where you can choose the sensitivity
     */
    public NormalizationFilter(DataSink sink, float customSensitivity){
        this( sink, customSensitivity, 0, 1, 2);
    }

    /**
     * A filter that normalizes the sensorData into usable input
     * @param customSensitivity custom value that is added to the base value
     */
    public NormalizationFilter(DataSink sink, float customSensitivity,  int xaxis, int yaxis, int zaxis){

        super(sink, xaxis, yaxis, zaxis);

        mCustomSensitivity = customSensitivity;
    }

    /**
     * Normalizes the sensorData by multiplying it with a base sensitivity value that is modified by
     * a custom value set by the user
     * @param sensorData the data received by the sensor
     */
    public void normalize(float[] sensorData){
        sensorData[XAXIS] *= mCustomSensitivity;
        sensorData[YAXIS] *= mCustomSensitivity;
        sensorData[ZAXIS] *= mCustomSensitivity;
    }

    public void setCustomSensitivity(float customValue){
        mCustomSensitivity = customValue;
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
}
