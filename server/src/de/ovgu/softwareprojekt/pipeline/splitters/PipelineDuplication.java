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
     * @param sink
     */
    public void removeDataSink(NetworkDataSink sink){
        mDataSinks.remove(sink);
    }

    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData, float userSensitivity) {
        if(mDataSinks.size() == 0)
            return;

        // the first time, we dont need to copy the data
        mDataSinks.get(0).onData(networkDevice, sensorData.clone(), userSensitivity);

        // all following times, clone the data
        for(int i = 1; i < mDataSinks.size(); i++)
            mDataSinks.get(i).onData(networkDevice, sensorData.clone(), userSensitivity);
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
