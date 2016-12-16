package de.ovgu.softwareprojekt.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Created by Ulrich on 28.11.2016.
 */
public class AverageMovementFilter extends AbstractFilter {

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

    /**
     * A filter that uses the mAverage movement in order to create a smooth movement
     *
     * @param avgSampSize how many values should be used to calculate the average
     * @param dataSink    where to put the filtered data
     */
    public AverageMovementFilter(int avgSampSize, NetworkDataSink dataSink) {
        this(avgSampSize, dataSink, 0, 1, 2);
    }

    /**
     * A filter that uses the mAverage movement in order to create a smooth movement
     *
     * @param avgSampSize how many values should be used to calculate the average
     * @param dataSink    where to put the filtered data
     * @param xaxis
     * @param yaxis
     * @param zaxis
     */
    public AverageMovementFilter(int avgSampSize, NetworkDataSink dataSink, int xaxis, int yaxis, int zaxis) {
        super(dataSink, xaxis, yaxis, zaxis);

        //The sample Size must be bigger than 0 or we would not create an average at all or even divide by zero
        assert avgSampSize > 0;

        mAverageSampleSize = avgSampSize;
        mAverage = new float[mAverageSampleSize][3];
        mIndexPointer = 0;
    }

    /**
     * turns the values of the given array into more useful ones
     *
     * @param rawData hopefully the SensorData.data array
     */
    private void filter(float[] rawData) {

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

    /**
     * Called when the next element should be filtered
     *
     * @param sensorData sensor data to process
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData) {
        filter(sensorData.data);

        mDataSink.onData(origin, sensorData);
    }

}
