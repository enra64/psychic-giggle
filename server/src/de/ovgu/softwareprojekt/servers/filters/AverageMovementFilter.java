package de.ovgu.softwareprojekt.servers.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorData;

import java.io.IOException;

/**
 * Created by Ulrich on 28.11.2016.
 */
public class AverageMovementFilter implements DataSink, DataSource{

    /**
     * Target data sink
     */
    private DataSink mDataSink;

    /**
     * These values call the wanted axis index of the sent rawData
     */
    private final int XAXIS, YAXIS, ZAXIS;

    //TODO: Decide later if filter(rawData) should only change rawData itself or return a new array
    //private float[] filteredData = new float[3];
    /**
     * This value describes how many of the previous inputs are used to calculate the mAverage movement
     */
    private final int mAverageSampleSize;

    /**
     * This 2D-Array uses an array for each sensor axis to calculate the mAverage movement
     */
    private float[][] mAverage;

    /**
     * mIndexPointer is a pointer index which loops around the first array of mAverage in order to overwrite the oldest
     * mAverage value
     */
    private int mIndexPointer; //used for mAverage output

    //TODO: find the best sensitivity
    /**
     * A base value which multiplies the gyroscope value to turn it into useful values
     */
    private final float SENSITIVITY = 40f;

    /**
     * Allows the user to change the base value SENSITIVITY by adding or subtracting this value
     */
    private float mCustomSensitivity = 0f;

    /**
     * Standard AverageMovementFilter that uses standard axes distribution X=0, Y=1, Z=2
     * @param avgSampSize how many samples should be used to calculate mAverage
     */
    public AverageMovementFilter(int avgSampSize, DataSink dataSink){
        this(avgSampSize,dataSink ,0, 1, 2);
    }

    /**
     * A filter that uses the mAverage movement in order to create a smooth movement
     * @param avgSampSize how many samples should be used to calculate mAverage
     * @param xaxis
     * @param yaxis
     * @param zaxis
     */
    public AverageMovementFilter(int avgSampSize, DataSink dataSink ,int xaxis, int yaxis, int zaxis) {
        XAXIS = xaxis;
        YAXIS = yaxis;
        ZAXIS = zaxis;

        //The sample Size must be bigger than 0 or else we would get an endless loop
        assert avgSampSize > 0;

        mAverageSampleSize = avgSampSize;
        mAverage = new float[mAverageSampleSize][3];
        mIndexPointer = 0;
        mCustomSensitivity = 0f;
        mDataSink = dataSink;
    }

    /**
     * turns the values of the given array into more useful ones
     * @param rawData hopefully the SensorData.data array
     */
    //TODO: Maybe return a float[] anyway?
    private void filter(float[] rawData) {
        //TODO: filter value should be customizable
        rawData[XAXIS] *= (SENSITIVITY + mCustomSensitivity);
        rawData[YAXIS] *= (SENSITIVITY + mCustomSensitivity);
        rawData[ZAXIS] *= (SENSITIVITY + mCustomSensitivity);

        //enter new gyroscope values
        mAverage[mIndexPointer][0] = rawData[XAXIS];
        mAverage[mIndexPointer][1] = rawData[YAXIS];
        mAverage[mIndexPointer][2] = rawData[ZAXIS];

        //create an 0f filled array used to calculate the mAverage later
        float[] filteredData = new float[rawData.length];

        //create mAverage of all values in the sample size
        for (int i = 0; i < mAverageSampleSize; i++) {
            filteredData[XAXIS] += mAverage[i][XAXIS];
            filteredData[YAXIS] += mAverage[i][YAXIS];
            filteredData[ZAXIS] += mAverage[i][ZAXIS];
        }
        mIndexPointer++;

        //TODO: If there is a bug, probably here lol
        System.arraycopy(filteredData, 0, rawData, 0, rawData.length);

        //mIndexPointer rotates through the array
        if (mIndexPointer == mAverageSampleSize) {
            mIndexPointer = 0;
        }

        //calculate mAverage
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] /= mAverageSampleSize;
        }
    }

    @Override
    public void onData(SensorData data) {
        filter(data.data);

        mDataSink.onData(data);
    }

    @Override
    public void setDataSink(DataSink sink) {
        mDataSink = sink;
    }

    @Override
    public void start() throws IOException {

    }

    @Override
    public void close() {

    }

    //TODO: user sensitivity feature in app
    public void setmCustomSensitivity(float customValue){
        mCustomSensitivity = customValue;
    }
}
