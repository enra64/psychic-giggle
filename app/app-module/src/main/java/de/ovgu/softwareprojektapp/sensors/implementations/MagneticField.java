package de.ovgu.softwareprojektapp.sensors.implementations;


import android.content.Context;
import android.hardware.Sensor;

import java.io.IOException;

import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.SetSensorSpeed;
import de.ovgu.softwareprojektapp.sensors.AbstractSensor;

/**
 * {@link DataSource} feeding MagneticField data
 */
public class MagneticField extends AbstractSensor {
    /**
     * Create an accelerometer; does not start anything yet, use {@link #start()} or {@link #setRunning(boolean)}
     * to start receiving events
     *
     * @param context    android system context needed for sensors
     */
    public MagneticField(Context context) {
        super(context, Sensor.TYPE_MAGNETIC_FIELD, SensorType.MagneticField);
    }

    /**
     * Static sensor speed definition for all MagneticField instances. defaults to sensor_delay_game.
     */
    private static SetSensorSpeed.SensorSpeed mSensorSpeed = SetSensorSpeed.SensorSpeed.SENSOR_DELAY_GAME;

    /**
     * update the sensor speed for all MagneticField instances
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
     * Get the sensor speed currently set for all MagneticField instances
     */
    @Override
    public SetSensorSpeed.SensorSpeed getSensorSpeed() {
        return mSensorSpeed;
    }
}