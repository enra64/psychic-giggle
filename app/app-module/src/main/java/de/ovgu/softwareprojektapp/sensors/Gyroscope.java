package de.ovgu.softwareprojektapp.sensors;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;

/**
 * {@link DataSource} for gyroscope data
 */
class Gyroscope extends AbstractSensor {
    /**
     * Create a gyroscope; does not start anything yet, use {@link #start()} or {@link #setRunning(boolean)}
     * to start receiving events
     *
     * @param context    android system context needed for sensors
     */
    Gyroscope(Context context) {
        super(context, Sensor.TYPE_GYROSCOPE, SensorType.Gyroscope);
    }
}