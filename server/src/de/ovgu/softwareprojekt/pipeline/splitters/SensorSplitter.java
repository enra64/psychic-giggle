package de.ovgu.softwareprojekt.pipeline.splitters;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.Collections;
import java.util.EnumMap;

/**
 * This filter splits data by sensor type to specified pipelines. Sensor data will not be copied, so sensor
 * data can only be sent to a single data sink.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SensorSplitter implements NetworkDataSink {
    /**
     * This map is used to store which data sink gets data from which sensor
     */
    private EnumMap<SensorType, NetworkDataSink> mNetworkDataSinkMap = new EnumMap<>(SensorType.class);

    /**
     * Add a new data sink which will receive all data sent from the specified sensor
     *
     * @param dataSink the data sink to receive all the data from {@code sensor}
     * @param sensor the sensor that should have its data forwarded to {@code dataSink}
     */
    public void addDataSink(SensorType sensor, NetworkDataSink dataSink) {
        mNetworkDataSinkMap.put(sensor, dataSink);
    }

    /**
     * Remove the specified sensor type. Data received from this sensor type will no longer be forwarded.
     *
     *  @param sensorType data received from this sensor type will no longer be forwarded.
     */
    public void remove(SensorType sensorType) {
        mNetworkDataSinkMap.remove(sensorType);
    }

    /**
     * Remove every instance of the specified data sink from this pipeline element.
     *
     * @param sink the sink that should no longer receive data from this {@link SensorSplitter}
     */
    public void remove(NetworkDataSink sink) {
        // remove all entries where the data sink is sink
        mNetworkDataSinkMap.values().removeAll(Collections.singleton(sink));
    }

    /**
     * Forward the data to the respective data sink
     *
     * @param origin          network device that sent the data
     * @param data            received data
     * @param userSensitivity sensitivity set by the user
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        if (mNetworkDataSinkMap.containsKey(data.sensorType))
            mNetworkDataSinkMap.get(data.sensorType).onData(origin, data, userSensitivity);
    }

    /**
     * Not needed for this splitter
     */
    @Override
    public void close() {
    }
}
