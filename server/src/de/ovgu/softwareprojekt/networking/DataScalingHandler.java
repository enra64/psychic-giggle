package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.pipeline.filters.NormalizationFilter;

import java.util.EnumMap;

/**
 * This class handles scaling all the data, because we may have to apply a different scaling factor to
 * all the incoming data
 */
public class DataScalingHandler implements NetworkDataSink {
    /**
     * Contains the scaling filters that have to be applied for each sensors
     */
    private EnumMap<SensorType, NormalizationFilter> mScalingFilters = new EnumMap<>(SensorType.class);

    /**
     * Create a new data scaling handler, which will multiply all incoming data with 40.
     *
     * @param outgoingDataSink where all outgoing data will go
     */
    DataScalingHandler(NetworkDataSink outgoingDataSink) {
        for (SensorType sensorType : SensorType.values())
            mScalingFilters.put(sensorType, new NormalizationFilter(outgoingDataSink, 50f, 10, 100));
    }

    /**
     * Changes the scaling factor that is applied to a sensor
     *
     * @param sensorType  the sensor that shall be affected
     * @param sensitivity sensitivity factor. applied before normalization
     */
    void setSensorSensitivity(SensorType sensorType, float sensitivity) {
        // get the normalization filter configured for this sensor
        NormalizationFilter sensorTypeFilter = mScalingFilters.get(sensorType);

        // update the sensitivity for this sensor type
        sensorTypeFilter.setCustomSensitivity(sensitivity);
    }

    /**
     * This changes the range a certain sensor will be in.
     *
     * @param targetRange maximum and -minimum of the resulting range for this sensor
     */
    void setOutputRange(SensorType sensor, float targetRange) {
        // get the normalization filter configured for this sensor
        NormalizationFilter sensorTypeFilter = mScalingFilters.get(sensor);

        // change the target range
        sensorTypeFilter.setTargetRange(targetRange);
    }

    /**
     * This changes the range a certain sensors data will be expected to be in
     *
     * @param sourceRange maximum and -minimum of the incoming range for this sensor
     */
    void setSourceRange(SensorType sensor, float sourceRange) {
        // get the normalization filter configured for this sensor
        NormalizationFilter sensorTypeFilter = mScalingFilters.get(sensor);

        // change the target range
        sensorTypeFilter.setSourceRange(sourceRange);
    }

    /**
     * handle incoming data
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        // get the normalization filter configured for this sensor
        NormalizationFilter sensorTypeFilter = mScalingFilters.get(sensorData.sensorType);

        // let it handle the sensordata; the scaled result will be pushed into the outgoing data sink
        // the user sensitivity parameter is ignored by NormalizationFilters!
        sensorTypeFilter.onData(origin, sensorData, userSensitivity);
    }

    @Override
    public void close() {
    }
}