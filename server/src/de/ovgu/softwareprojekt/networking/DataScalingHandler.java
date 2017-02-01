package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.EnumMap;

/**
 * This class handles scaling all the data, because we may have to apply a different scaling factor to
 * all the incoming data
 */
class DataScalingHandler implements NetworkDataSink {
    /**
     * Contains the scaling filters that have to be applied for each sensors
     */
    private EnumMap<SensorType, Float> mUserSensitivity = new EnumMap<>(SensorType.class);

    /**
     * Contains the scaling filters that have to be applied for each sensors
     */
    private EnumMap<SensorType, Float> mSensorRange = new EnumMap<>(SensorType.class);

    /**
     * The data sink that will receive all data entered here
     */
    private NetworkDataSink mOutgoingDataSink;


    /**
     * Create a new data scaling handler, which will multiply all incoming data with 40.
     *
     * @param outgoingDataSink where all outgoing data will go
     */
    DataScalingHandler(NetworkDataSink outgoingDataSink) {
        mOutgoingDataSink = outgoingDataSink;

        for (SensorType sensorType : SensorType.values())
            mUserSensitivity.put(sensorType, 50f);

    }

    /**
     * Changes the scaling factor that is applied to a sensor
     *
     * @param sensorType  the sensor that shall be affected
     * @param sensitivity sensitivity factor. applied before normalization
     */

    void setSensorUserSensitivity(SensorType sensorType, float sensitivity) {
        // get the normalization filter configured for this sensor
        mUserSensitivity.put(sensorType, sensitivity);
    }

    /**
     * This changes the range a certain sensors data will be expected to be in
     *
     * @param sensor      the sensor of which the source range should be modified
     * @param sourceRange maximum and -minimum of the incoming range for this sensor
     */

    void setSensorRange(SensorType sensor, float sourceRange) {
        mSensorRange.put(sensor, sourceRange);
    }

    /**
     * handle incoming data
     */
    @Override
    public void onData(NetworkDevice origin, SensorData sensorData, float userSensitivity) {
        // get the user sensitivity configured for this sensor
        float sensorSensitivity = mUserSensitivity.get(sensorData.sensorType);

        // forwardData the data
        mOutgoingDataSink.onData(origin, sensorData, sensorSensitivity);
    }

    /**
     * Close the DataScalingHandler.
     */
    @Override
    public void close() {
    }

    /**
     * Retrieve the sensor range for the requested sensor for this DataScalingHandler
     *
     * @param sensor the requested sensor
     * @return the sensor range as reported by android for the sensor, see <a href="https://developer.android.com/reference/android/hardware/Sensor.html#getMaximumRange()">android docs</a>
     */
    float getSensorRange(SensorType sensor) {
        return mSensorRange.get(sensor);
    }
}