package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This interface is used when you want to define a data splitter, or an end piece of the data pipeline that consumes the
 * incoming data. It may also be implemented in combination with {@link NetworkDataSource}, like in {@link de.ovgu.softwareprojekt.pipeline.filters.AbstractFilter},
 * to create a class that does not use but modify the data.
 * <p>
 * On the app side, the origin and the sensor sensitivity of data are not relevant, so {@link de.ovgu.softwareprojekt.DataSink} is used there.
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
