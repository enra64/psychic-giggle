package de.ovgu.softwareprojektapp.sensors.implementations;


import android.content.Context;
import android.hardware.Sensor;

import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojektapp.sensors.AbstractSensor;

/**
 * {@link DataSource} feeding accelerometer data
 */
public class Magnetometer extends AbstractSensor {
    /**
     * Create an accelerometer; does not start anything yet, use {@link #start()} or {@link #setRunning(boolean)}
     * to start receiving events
     *
     * @param context    android system context needed for sensors
     */
    public Magnetometer(Context context) {
        super(context, Sensor.TYPE_MAGNETIC_FIELD, SensorType.Magnetometer);
    }
}