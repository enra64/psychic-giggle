package de.ovgu.softwareprojekt.pipeline.filters;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This class multiplies the data by the incoming user sensitivity, and forwards the data with a user sensitivity of 1
 */
public class UserSensitivityMultiplicator extends AbstractFilter {
    /**
     * Apply the user sensitivity as a factor
     */
    public UserSensitivityMultiplicator(NetworkDataSink sink){
        super(sink);
    }

    /**
     * Apply the user sensitivity as a factor
     */
    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData, float v) {
        for(int i = 0; i < sensorData.data.length; i++)
            sensorData.data[i] *= v;

        // forward data
        mDataSink.onData(networkDevice, sensorData, 1);
    }
}
