package de.ovgu.softwareprojekt.pipeline.splitters;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This class combines the functionality of {@link SensorSplitter} and {@link ClientSplitter}, that is, data sinks can be
 * registered to be notified only of specified sensor type events from specified network devices
 */
public class ClientSensorSplitter {
    /**
     * The {@link ClientSplitter} used to split data by {@link NetworkDevice}
     */
    private ClientSplitter mClientSplitter = new ClientSplitter();

    /**
     * The {@link SensorSplitter} used to split data by {@link SensorType}
     */
    private SensorSplitter mSensorSplitter = new SensorSplitter();

    /**
     * Add a data sink to be notified of sensor data for a certain sensor from a specified network device
     * @param origin the network device that must have sent the data
     * @param sensor the sensor that must have sent the data
     * @param dataSink the data sink that receives <code>sensor</code> data from <code>origin</code>
     */
    public void addDataSink(NetworkDevice origin, SensorType sensor, NetworkDataSink dataSink){
        mSensorSplitter.addDataSink(sensor, dataSink);
        mClientSplitter.addDataSink(origin, mSensorSplitter);
    }

    /**
     * Remove a data sink from this pipeline element. It will no longer be notified of any data from this source.
     * @param dataSink the data sink
     */
    public void removeElement(NetworkDataSink dataSink){
        mSensorSplitter.remove(dataSink);
        mClientSplitter.remove(dataSink);
    }
}
