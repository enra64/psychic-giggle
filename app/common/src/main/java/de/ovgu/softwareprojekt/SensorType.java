package de.ovgu.softwareprojekt;

/**
 * This enum lists all known sensor types
 *
 */
// when adding a sensor type, remember to add its class to the sensor handler (at the bottom...)
public enum SensorType {
    Accelerometer,
    Gyroscope,
    Magnetometer,
    RotationVector
}
