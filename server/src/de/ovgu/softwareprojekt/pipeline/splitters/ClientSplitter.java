package de.ovgu.softwareprojekt.pipeline.splitters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.Collections;
import java.util.HashMap;

/**
 * This class can be used to split data from various devices to different pipelines. Sensor data will not be duplicated
 * or copied, so sensor data can only be sent to a single data sink.
 */
@SuppressWarnings("unused")
public class ClientSplitter implements NetworkDataSink {
    /**
     * This map is used to store which data sink gets data from which device
     */
    private HashMap<NetworkDevice, DataSink> mDataSinkMap = new HashMap<>();

    /**
     * This map is used to store which data sink gets data from which device
     */
    private HashMap<NetworkDevice, NetworkDataSink> mNetworkDataSinkMap = new HashMap<>();

    /**
     * Add a new data sink which will receive all data sent by the specified device
     */
    public void addDataSink(NetworkDevice source, DataSink dataSink){
        if(!mNetworkDataSinkMap.containsKey(source))
            mDataSinkMap.put(source, dataSink);
    }

    /**
     * Add a new data sink which will receive all data sent by the specified device
     */
    public void addDataSink(NetworkDevice source, NetworkDataSink dataSink){
        if(!mDataSinkMap.containsKey(source))
            mNetworkDataSinkMap.put(source, dataSink);
    }

    /**
     * Remove the specified device
     */
    public void remove(NetworkDevice device){
        mDataSinkMap.remove(device);
        mNetworkDataSinkMap.remove(device);
    }

    /**
     * Remove every instance of the specified data sink from this pipeline element
     */
    public void remove(DataSink sink){
        // remove all entries where the data sink is sink
        mDataSinkMap.values().removeAll(Collections.singleton(sink));
    }

    /**
     * Remove every instance of the specified data sink from this pipeline element
     */
    public void remove(NetworkDataSink sink){
        // remove all entries where the data sink is sink
        mNetworkDataSinkMap.values().removeAll(Collections.singleton(sink));
    }

    @Override
    public void onData(NetworkDevice origin, SensorData data) {
        if(mDataSinkMap.containsKey(origin))
            mDataSinkMap.get(origin).onData(data);
        if(mNetworkDataSinkMap.containsKey(origin))
            mNetworkDataSinkMap.get(origin).onData(origin, data);
    }

    @Override
    public void close() {
    }
}
