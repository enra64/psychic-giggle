package de.ovgu.softwareprojekt.pipeline.splitters;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This splitter will clone the pipeline data to each added data sink.
 */
@SuppressWarnings("unused")
public class PipelineDuplication implements NetworkDataSink {
    /**
     * The list of our data sinks.
     */
    private List<NetworkDataSink> mDataSinks = new ArrayList<>();

    /**
     * Add a data sink. It will get any and all SensorData objects received by this PipelineDuplication element
     *
     * @param sink the data sink that should from now on also receive any data received by this {@link PipelineDuplication} instance
     */
    public void addDataSink(NetworkDataSink sink) {
        mDataSinks.add(sink);
    }

    /**
     * Remove a data sink from this PipelineDuplication element. It will no longer receive data from here
     *
     * @param sink data sink that should no longer receive data
     */
    public void removeDataSink(NetworkDataSink sink) {
        mDataSinks.remove(sink);
    }

    /**
     * onData is called whenever new data is to be processed
     *
     * @param origin          the network device which sent the data
     * @param sensorData            the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        // copy the data for each further recipient
        mDataSinks.forEach(sink -> sink.onData(origin, sensorData.clone(), userSensitivity));
    }

    /**
     * Closing this will also close any directly following pipeline elements
     */
    @Override
    public void close() {
        for (NetworkDataSink sink : mDataSinks)
            sink.close();
    }
}
