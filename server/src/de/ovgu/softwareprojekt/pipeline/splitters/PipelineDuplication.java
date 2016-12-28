package de.ovgu.softwareprojekt.pipeline.splitters;

import de.ovgu.softwareprojekt.NetworkDataSink;
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
     */
    public void addDataSink(NetworkDataSink sink){
        mDataSinks.add(sink);
    }

    /**
     * Remove a data sink from this PipelineDuplication element. It will no longer receive data from here
     * @param sink data sink that should no longer receive data
     */
    public void removeDataSink(NetworkDataSink sink){
        mDataSinks.remove(sink);
    }

    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData, float userSensitivity) {
        // copy the data for each further recipient
        mDataSinks.forEach(sink -> sink.onData(networkDevice, sensorData.clone(), userSensitivity));
    }

    /**
     * Closing this will also close any directly following pipeline elements
     */
    @Override
    public void close() {
        for(NetworkDataSink sink : mDataSinks)
            sink.close();
    }
}
