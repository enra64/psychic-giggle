package de.ovgu.softwareprojekt.pipeline.filters;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This filter replaces any negative values with its absolute value.
 */
public class AbsoluteFilter extends AbstractFilter {
    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData) {
        for(int i = 0; i < sensorData.data.length; i++)
            if(sensorData.data[i] < 0)
                sensorData.data[i] *= -1;
        mDataSink.onData(networkDevice, sensorData);
    }
}
