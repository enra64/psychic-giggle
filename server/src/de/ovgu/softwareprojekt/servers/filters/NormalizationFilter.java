package de.ovgu.softwareprojekt.servers.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;

import java.io.IOException;

/**
 * Created by Ulrich on 29.11.2016.
 *
 * This class receives the sensorData and normalizes (?) it to a usable value by multiplying it
 * with a base sensitivity value which can be changed by subtracting or adding a custom value to
 * the sensitivity
 */
public class NormalizationFilter extends AbstractFilter {

    //TODO: find the best sensitivity
    /**
     * A base value which multiplies the gyroscope value to turn it into useful values
     */
    private final float SENSITIVITY;

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
        this(40f, 0f, dataSink, 0, 1, 2);
    }

    /**
     * A filter that normalizes the sensorData into usable input
     * @param sensitivity base value that normalizes the sensor data, cannot be negative
     * @param customSensitivity custom value that is added to the base value
     */
    public NormalizationFilter(float sensitivity, float customSensitivity, DataSink sink, int xaxis, int yaxis, int zaxis){

        super(sink, xaxis, yaxis, zaxis);

        //It is basicly pointless to use 0 or negative values
        //TODO: assert necessary?
        assert sensitivity > 0;

        SENSITIVITY = sensitivity;
        mCustomSensitivity = customSensitivity;
    }

    /**
     * Normalizes the sensorData by multiplying it with a base sensitivity value that is modified by
     * a custom value set by the user
     * @param sensorData the data received by the sensor
     */
    public void normalize(float[] sensorData){
        sensorData[XAXIS] *= (SENSITIVITY + mCustomSensitivity);
        sensorData[YAXIS] *= (SENSITIVITY + mCustomSensitivity);
        sensorData[ZAXIS] *= (SENSITIVITY + mCustomSensitivity);
    }

    //TODO: user sensitivity feature in app
    public void setmCustomSensitivity(float customValue){
        mCustomSensitivity = customValue;
    }

    @Override
    public void onData(SensorData sensorData) {
        normalize(sensorData.data);
        mDataSink.onData(sensorData);
    }
}
