package de.ovgu.softwareprojekt;

/**
 * All sensor types known to the framework.
 */
// WARNING: THE SENSOR CLASS NAME (FOR ANDROID)  MUST MATCH THE ENUM VALUE _EXACTLY_!
public enum SensorType {
    Accelerometer,
    AmbientTemperature,
    GameRotationVector,
    Gravity,
    Gyroscope,
    GyroscopeUncalibrated,
    Light,
    LinearAcceleration,
    MagneticField,
    MagneticFieldUncalibrated,
    Orientation,
    Pressure,
    Proximity,
    RelativeHumidity,
    RotationVector
}
