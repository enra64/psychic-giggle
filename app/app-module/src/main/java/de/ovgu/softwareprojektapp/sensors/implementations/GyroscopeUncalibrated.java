package de.ovgu.softwareprojektapp.sensors.implementations;


import android.content.Context;
import android.hardware.Sensor;

import java.io.IOException;

import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.SetSensorSpeed;
import de.ovgu.softwareprojektapp.sensors.AbstractSensor;

/**
 * {@link DataSource} feeding {@link Sensor#TYPE_GYROSCOPE_UNCALIBRATED} data
 */
public class GyroscopeUncalibrated extends AbstractSensor {
    /**
     * Create an{@link GyroscopeUncalibrated} sensor; does not start anything yet, use {@link #start()} or {@link #setRunning(boolean)}
     * to start receiving events
     *
     * @param context android system context needed for sensors
     */
    public GyroscopeUncalibrated(Context context) {
        super(context, Sensor.TYPE_GYROSCOPE_UNCALIBRATED, SensorType.GyroscopeUncalibrated);
    }

    /**
     * Static sensor speed definition for all{@link GyroscopeUncalibrated} instances. defaults to sensor_delay_game.
     */
    private static SetSensorSpeed.SensorSpeed mSensorSpeed = SetSensorSpeed.SensorSpeed.SENSOR_DELAY_GAME;

    /**
     * update the sensor speed for all{@link GyroscopeUncalibrated} instances
     *
     * @param targetSpeed the speed the sensor should run at after this call
     * @throws IOException if the sensor could not be restarted
     */
    public void setSensorSpeed(SetSensorSpeed.SensorSpeed targetSpeed) throws IOException {
        // only restart if the sensor was registered
        boolean restart = isRegistered();

        // unregister the listener
        close();

        // change the speed
        mSensorSpeed = targetSpeed;

        // re-register the listener
        if(restart) start();
    }

    /**
     * Get the sensor speed currently set for all {@link GyroscopeUncalibrated} instances
     */
    @Override
    public SetSensorSpeed.SensorSpeed getSensorSpeed() {
        return mSensorSpeed;
    }
}