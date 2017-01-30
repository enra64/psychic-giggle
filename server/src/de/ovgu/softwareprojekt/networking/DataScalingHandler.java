package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.pipeline.filters.NormalizationFilter;

import java.util.EnumMap;

/**
 * This class handles scaling all the data, because we may have to apply a different scaling factor to
 * all the incoming data
 */
class DataScalingHandler implements NetworkDataSink {
    /**
     * Contains the scaling filters that have to be applied for each sensors
     */
    private EnumMap<SensorType, Float> mScalingFilters = new EnumMap<>(SensorType.class);

    /**
     * Create a new data scaling handler, which will multiply all incoming data with 40.
     *
     * @param outgoingDataSink where all outgoing data will go
     */
    DataScalingHandler(NetworkDataSink outgoingDataSink) {
        for (SensorType sensorType : SensorType.values())
            mScalingFilters.put(sensorType, 10f);
    }

    /**
     * Changes the scaling factor that is applied to a sensor
     *
     * @param sensorType  the sensor that shall be affected
     * @param sensitivity sensitivity factor. applied before normalization
     */
    void setSensorSensitivity(SensorType sensorType, float sensitivity) {

        // update the sensitivity for this sensor type
        mScalingFilters.put(sensorType, sensitivity);
    }


    /**
     * This changes the range a certain sensors data will be expected to be in
     *
     * @param sensor      the sensor of which the source range should be modified
     * @param sourceRange maximum and -minimum of the incoming range for this sensor
     */
    void setSourceRange(SensorType sensor, float sourceRange) {
    }

    /**
     * handle incoming data
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
 
    }

    @Override
    public void close() {
    }
}