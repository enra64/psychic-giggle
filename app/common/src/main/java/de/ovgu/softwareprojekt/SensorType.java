package de.ovgu.softwareprojekt;

/**
 * All sensor types known to the framework.
 *
 */
// WARNING: THE SENSOR CLASS NAME (FOR ANDROID)  MUST MATCH THE ENUM VALUE _EXACTLY_!
public enum SensorType {
    Accelerometer,
    Gyroscope,
    Magnetometer,
    LinearAcceleration, 
    RotationVector,
    Orientation,
    GameRotationVector,
    Gravity
}
