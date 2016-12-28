package de.ovgu.softwareprojekt.pipeline.splitters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.Collections;
import java.util.EnumMap;

/**
 * This filter splits data by sensor type to specified pipelines. Sensor data will not be duplicated or copied, so sensor
 * data can only be sent to a single data sink.
 */
public class SensorSplitter implements NetworkDataSink {
    /**
     * This map is used to store which data sink gets data from which sensor
     */
    private EnumMap<SensorType, DataSink> mDataSinkMap = new EnumMap<>(SensorType.class);

    /**
     * This map is used to store which data sink gets data from which device
     */
    private EnumMap<SensorType, NetworkDataSink> mNetworkDataSinkMap = new EnumMap<>(SensorType.class);

    /**
     * Add a new data sink which will receive all data sent by the specified sensor
     */
    public void addDataSink(SensorType sensor, DataSink dataSink) {
        if(!mNetworkDataSinkMap.containsKey(sensor))
            mDataSinkMap.put(sensor, dataSink);
    }

    /**
     * Add a new data sink which will receive all data sent by the specified device
     */
    public void addDataSink(SensorType sensor, NetworkDataSink dataSink) {
        if(!mDataSinkMap.containsKey(sensor))
            mNetworkDataSinkMap.put(sensor, dataSink);
    }

    /**
     * Remove the specified sensor type
     */
    public void remove(SensorType device) {
        mDataSinkMap.remove(device);
        mNetworkDataSinkMap.remove(device);
    }

    /**
     * Remove every instance of the specified data sink from this pipeline element
     */
    public void remove(DataSink sink) {
        // remove all entries where the data sink is sink
        mDataSinkMap.values().removeAll(Collections.singleton(sink));
    }

    /**
     * Remove every instance of the specified data sink from this pipeline element
     */
    public void remove(NetworkDataSink sink) {
        // remove all entries where the data sink is sink
        mNetworkDataSinkMap.values().removeAll(Collections.singleton(sink));
    }

    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        if(mNetworkDataSinkMap.containsKey(data.sensorType))
            mNetworkDataSinkMap.get(data.sensorType).onData(origin, data, userSensitivity);
        if(mDataSinkMap.containsKey(data.sensorType))
            mDataSinkMap.get(data.sensorType).onData(data);
    }

    @Override
    public void close() {
    }
}
