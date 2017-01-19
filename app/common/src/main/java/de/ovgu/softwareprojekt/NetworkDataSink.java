package de.ovgu.softwareprojekt;

import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Interface for sensor data sinks, with an added origin parameter do differentiate between data
 * sources, and a userSensitivity parameter to inform of the sensitivity selected by the user for the
 * sensor.
 */
@SuppressWarnings("WeakerAccess")
public interface NetworkDataSink {
    /**
     * onData is called whenever new data is to be processed
     *
     * @param origin          the network device which sent the data
     * @param data            the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings for the sensor
     *                        the data is from
     */
    void onData(NetworkDevice origin, SensorData data, float userSensitivity);

    /**
     * called when the sink is no longer used and should no longer require resources
     */
    void close();
}
