package de.ovgu.softwareprojekt.pipeline.filters;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This class multiplies the data by the incoming user sensitivity, and forwards the data with a user sensitivity of 1
 */
public class UserSensitivityMultiplicator extends AbstractFilter {
    /**
     * Apply the user sensitivity as a factor
     *
     * @param sink the sink that should receive the multiplied data.
     */
    public UserSensitivityMultiplicator(NetworkDataSink sink) {
        super(sink);
    }

    /**
     * Apply the user sensitivity as a factor
     *
     * @param origin          the network device which sent the data
     * @param sensorData      the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        for (int i = 0; i < sensorData.data.length; i++)
            sensorData.data[i] *= userSensitivity;

        // forward data
        mDataSink.onData(origin, sensorData, 1);
    }
}
