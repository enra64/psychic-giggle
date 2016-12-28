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
@SuppressWarnings({"unused", "WeakerAccess"})
public class ClientSplitter implements NetworkDataSink {
    /**
     * This map is used to store which data sink gets data from which device
     */
    private HashMap<NetworkDevice, NetworkDataSink> mNetworkDataSinkMap = new HashMap<>();

    /**
     * Add a new data sink which will receive all incoming data sent by the specified device
     */
    public void addDataSink(NetworkDevice source, NetworkDataSink dataSink){
        mNetworkDataSinkMap.put(source, dataSink);
    }

    /**
     * Remove the specified device. Data received from this device cannot be forwarded after calling this method
     */
    public void remove(NetworkDevice device){
        mNetworkDataSinkMap.remove(device);
    }

    /**
     * Remove every instance of the specified data sink from this pipeline element
     */
    public void remove(NetworkDataSink sink){
        // remove all entries where the data sink is sink
        mNetworkDataSinkMap.values().removeAll(Collections.singleton(sink));
    }

    /**
     * Forward incoming data to the appropriate recipient
     * @param origin network device that sent the data
     * @param data received data
     * @param userSensitivity sensitivity set by the user
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        if(mNetworkDataSinkMap.containsKey(origin))
            mNetworkDataSinkMap.get(origin).onData(origin, data, userSensitivity);
    }

    /**
     * Unnecessary for this implementation
     */
    @Override
    public void close() {
    }
}
