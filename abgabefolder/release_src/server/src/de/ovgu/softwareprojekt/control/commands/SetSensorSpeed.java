package de.ovgu.softwareprojekt.control.commands;

import de.ovgu.softwareprojekt.SensorType;

/**
 * Command for changing the requested speed of a sensor, e.g. the update frequency
 */
@SuppressWarnings("WeakerAccess")
public class SetSensorSpeed extends AbstractCommand {
    /**
     * These are the sensor speeds made available by the android framework.
     *
     * @see <a href="https://developer.android.com/reference/android/hardware/SensorManager.html#SENSOR_DELAY_FASTEST">documentation</a>
     */

    public enum SensorSpeed {
        SENSOR_DELAY_FASTEST,
        SENSOR_DELAY_GAME,
        SENSOR_DELAY_NORMAL,
        SENSOR_DELAY_UI
    }

    /**
     * The sensor whose speed is to be changed to {@link #sensorSpeed}
     */
    public SensorType affectedSensor;

    /**
     * Requested speed for {@link #affectedSensor}
     */
    public SensorSpeed sensorSpeed;

    /**
     * Create a new command for setting sensor speed
     */
    public SetSensorSpeed(SensorType affected, SensorSpeed speed) {
        super(CommandType.SetSensorSpeed);
        affectedSensor = affected;
        sensorSpeed = speed;
    }
}
