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
 * {@link DataSource} feeding accelerometer data
 */
class Accelerometer extends AbstractSensor {
    /**
     * Create an accelerometer; does not start anything yet, use {@link #start()} or {@link #setRunning(boolean)}
     * to start receiving events
     *
     * @param context    android system context needed for sensors
     */
    Accelerometer(Context context) {
        super(context, Sensor.TYPE_ACCELEROMETER, SensorType.Accelerometer);
    }

    @Override
    public SensorType getSensorType() {
        return SensorType.Accelerometer;
    }
}