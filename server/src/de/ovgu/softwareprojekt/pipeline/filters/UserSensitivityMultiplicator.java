package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This class multiplies the data by the incoming user sensitivity, and forwards the data with a user sensitivity of 1f.
 */
public class UserSensitivityMultiplicator extends AbstractFilter {
    /**
     * Apply the user sensitivity as a factor, replacing the sensitivity value with <code>1f</code>.
     *
     * @param sink the sink that should receive the multiplied data. if null, a {@link NetworkDataSink} must be set
     *             before using the filter with {@link #setDataSink(NetworkDataSink)}
     */
    public UserSensitivityMultiplicator(@Nullable NetworkDataSink sink) {
        super(sink);
    }

    /**
     * Apply the user sensitivity as a factor, replacing the sensitivity value with <code>1f</code>.
     * A {@link NetworkDataSink} must be set before using the filter, see {@link #setDataSink(NetworkDataSink)}
     */
    public UserSensitivityMultiplicator() {
        this(null);
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

        // forwardData data
        forwardData(origin, sensorData, 1);
    }
}
