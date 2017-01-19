package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.util.RingBuffer;

import java.util.Arrays;

/**
 * This filter replaces incoming sensor data with the average over the last n values.
 */
public class AveragingFilter extends AbstractFilter {
    /**
     * The ring buffer we use for averaging over past values
     */
    private RingBuffer<float[]> mBuffer;

    /**
     * Create a new average movement filter without a configured data sink.
     * {@link #setDataSink(NetworkDataSink)} must be called before operations may begin
     *
     * @param avgSampSize how many values should be used to calculate the average
     */
    public AveragingFilter(int avgSampSize) {
        // call this constructor with null data sink
        this(avgSampSize, null);
    }

    /**
     * A filter that uses the Average movement in order to create a smooth movement
     *
     * @param avgSampSize how many values should be used to calculate the average
     * @param dataSink    either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *                    must be called prior to starting operations.
     */
    public AveragingFilter(int avgSampSize, @Nullable NetworkDataSink dataSink) {
        super(dataSink);

        //The sample Size must be bigger than 0 or we would not create an average at all or even divide by zero
        assert avgSampSize > 0;

        mBuffer = new RingBuffer<>(avgSampSize, new float[]{0, 0, 0});
    }

    /**
     * Replaces the incoming array with the average of it and its predecessors
     *
     * @param rawData hopefully the SensorData.data array
     */
    private void filter(float[] rawData) {
        //enter new gyroscope values
        mBuffer.add(rawData.clone());

        // the raw data is no longer needed, so we can use it as a temporary variable
        Arrays.fill(rawData, 0);

        // sum all values in mBuffer
        for (int bufferIndex = 0; bufferIndex < mBuffer.size(); bufferIndex++)
            for (int dataIndex = 0; dataIndex < rawData.length; dataIndex++)
                rawData[dataIndex] += mBuffer.get(bufferIndex)[dataIndex];

        // average the values by dividing by their count
        for (int i = 0; i < rawData.length; i++)
            rawData[i] /= mBuffer.size();
    }

    /**
     * Called when the next element should be filtered
     *
     * @param sensorData      sensor data to process
     * @param origin          network device that sent this data
     * @param userSensitivity sensitivity set by the user
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        filter(sensorData.data);

        mDataSink.onData(origin, sensorData, userSensitivity);
    }

}
