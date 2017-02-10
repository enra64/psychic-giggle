package de.ovgu.softwareprojektapp.sensors;

import android.content.Context;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojektapp.R;

/**
 * This class holds utilities for handling the sensors
 */
public class SensorUtil {
    /**
     * Map from SensorType to a fitting sensor name using the correct locale
     *
     * @param sensor the sensor of which to get the name
     * @return string representation of the sensor name
     */
    public static String getSensorName(Context context, SensorType sensor) {
        switch (sensor){
            case Accelerometer:
                return context.getString(R.string.sensor_accelerometer);
            case AmbientTemperature:
                return context.getString(R.string.sensor_ambient_temperature);
            case GameRotationVector:
                return context.getString(R.string.sensor_game_rotation_vector);
            case Gravity:
                return context.getString(R.string.sensor_gravity);
            case Gyroscope:
                return context.getString(R.string.sensor_gyroscope);
            case GyroscopeUncalibrated:
                return context.getString(R.string.sensor_gyroscope_uncalibrated);
            case Light:
                return context.getString(R.string.sensor_light);
            case LinearAcceleration:
                return context.getString(R.string.sensor_linear_acceleration);
            case MagneticField:
                return context.getString(R.string.sensor_magnetic_field);
            case MagneticFieldUncalibrated:
                return context.getString(R.string.sensor_magnetic_field_uncalibrated);
            case Orientation:
                return context.getString(R.string.sensor_orientation);
            case Pressure:
                return context.getString(R.string.sensor_pressure);
            case Proximity:
                return context.getString(R.string.sensor_proximity);
            case RelativeHumidity:
                return context.getString(R.string.sensor_relative_humidity);
            case RotationVector:
                return context.getString(R.string.sensor_rotation_vector);
            default:
                return "Sensor not known";
        }
    }
}
