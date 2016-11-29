package de.ovgu.softwareprojekt.servers.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorData;

import java.io.IOException;

/**
 * Created by Ulrich on 29.11.2016.
 *
 * This class receives the sensorData and normalizes (?) it to a usable value by multiplying it
 * with a base sensitivity value which can be changed by subtracting or adding a custom value to
 * the sensitivity
 */
public class NormalizationFilter implements DataSink, DataSource {

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
     * Target data sink
     */
    private DataSink mDataSink;

    /**
     * These values call the wanted axis index of the sent rawData
     */
    private final int XAXIS, YAXIS, ZAXIS;

    /**
     * standard constructor that chooses 40 as base sensitivity and 0 as custom sensitivity
     */
    public NormalizationFilter(DataSink sink){
        this(40f, 0f, sink, 0, 1, 2);
    }

    /**
     * standard constructor where you can choose the sensitivity
     */
    public NormalizationFilter(DataSink sink, float sensitivity, float customSensitivity){
        this(sensitivity, customSensitivity, sink, 0, 1, 2);
    }

    /**
     * A filter that normalizes the sensorData into usable input
     * @param sensitivity base value that normalizes the sensor data, cannot be negative
     * @param customSensitivity custom value that is added to the base value
     */
    public NormalizationFilter(float sensitivity, float customSensitivity, DataSink sink, int xaxis, int yaxis, int zaxis){

        //It is basicly pointless to use 0 or negative values
        //TODO: assert necessary?
        assert sensitivity > 0;

        mDataSink = sink;
        SENSITIVITY = sensitivity;
        mCustomSensitivity = customSensitivity;

        XAXIS = xaxis;
        YAXIS = yaxis;
        ZAXIS = zaxis;
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
    public void setCustomSensitivity(float customValue){
        mCustomSensitivity = customValue;
    }

    @Override
    public void onData(SensorData sensorData) {
        normalize(sensorData.data);
        mDataSink.onData(sensorData);
    }

    @Override
    public void setDataSink(DataSink dataSink) {
        mDataSink = dataSink;
    }

    @Override
    public void start() throws IOException {

    }

    @Override
    public void close() {

    }
}
