package de.ovgu.softwareprojekt.pipeline;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.pipeline.filters.AbstractFilter;
import de.ovgu.softwareprojekt.util.RingBuffer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This {@link AbstractFilter} subclass forwards incoming data without changes, but measures how often it receives data.
 */
public class ThroughputMeasurer extends AbstractFilter {
    /**
     * Timer used for resetting the throughput measurement after each measurement block
     */
    private final Timer mThroughputTimer = new Timer();

    /**
     * Incremented whenever a new point comes in, and reset once per block
     */
    private int mThroughputCounter = 0;

    /**
     * The storage used for storing data throughput counts for different time spans
     */
    private RingBuffer<Integer> mDataThroughputStorage;

    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        mThroughputCounter++;
        mDataSink.onData(origin, data, userSensitivity);
    }

    /**
     * Returns the measured throughput
     *
     * @return sum of packages received in the blocks divided by the amount of blocks
     */
    public float getThroughput() {
        int sum = 0;
        for (int i = 0; i < mDataThroughputStorage.size(); i++)
            sum += mDataThroughputStorage.get(i);

        if(mDataThroughputStorage.size() == 0)
            return 0;

        return sum / mDataThroughputStorage.size();
    }

    /**
     * Close this ThroughputMeasurer.
     */
    @Override
    public void close() {
        super.close();
        mThroughputTimer.cancel();
    }

    /**
     * Create a new ThroughputMeasurer.
     *
     * @param blockLength how long a block is (ms)
     * @param averageSize how many blocks are used to average over
     */
    public ThroughputMeasurer(long blockLength, int averageSize) {
        mDataThroughputStorage = new RingBuffer<>(averageSize, 0);
        // reset pps once per second
        mThroughputTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mDataThroughputStorage.add(mThroughputCounter);
                mThroughputCounter = 0;
            }
        }, 0, blockLength);
    }
}
