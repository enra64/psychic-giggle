package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Interface for sensor data sinks, with an added origin parameter do differentiate between data
 * sources.
 */
@SuppressWarnings("WeakerAccess")
public interface NetworkDataSink {

    /**
     * onData is called whenever new data is to be processed
     *
     * @param origin the network device which sent the data
     * @param data the sensor data
     */
    void onData(NetworkDevice origin, SensorData data);

    /**
     * called when the sink is no longer used and should no longer require resources
     */
    void close();
}
