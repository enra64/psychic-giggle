package de.ovgu.softwareprojekt.filters;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This class combines the functionality of {@link SensorSplitter} and {@link ClientSplitter}, that is, data sinks can be
 * registered to be notified only of specified sensor type events from specified network devices
 */
public class ClientSensorSplitter {
    private ClientSplitter mClientSplitter = new ClientSplitter();
    private SensorSplitter mSensorSplitter = new SensorSplitter();

    public void addDataSink(NetworkDevice origin, SensorType sensor, NetworkDataSink dataSink){
        mSensorSplitter.addDataSink(sensor, dataSink);
        mClientSplitter.addDataSink(origin, mSensorSplitter);
    }

    public void removeElement(NetworkDataSink dataSink){
        mSensorSplitter.remove(dataSink);
        mClientSplitter.remove(dataSink);
    }
}
