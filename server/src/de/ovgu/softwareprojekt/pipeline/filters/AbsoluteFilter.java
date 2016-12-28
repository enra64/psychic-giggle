package de.ovgu.softwareprojekt.pipeline.filters;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This filter replaces any negative sensor data values with their absolute.
 */
@SuppressWarnings("unused")
public class AbsoluteFilter extends AbstractFilter {
    /**
     * Replace any negative values with its absolute value.
     */
    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData, float userSensitivity) {
        // i think this is the fastest way to replace anything negative with the absolute
        for(int i = 0; i < sensorData.data.length; i++)
            // only act if the number is smaller than zero
            if(sensorData.data[i] < 0)
                // simple negative assignment, no fp multiplication
                sensorData.data[i] = -sensorData.data[i];

        // forward data
        mDataSink.onData(networkDevice, sensorData, userSensitivity);
    }
}
