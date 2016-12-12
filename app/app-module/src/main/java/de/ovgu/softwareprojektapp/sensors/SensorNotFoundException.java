package de.ovgu.softwareprojektapp.sensors;

import de.ovgu.softwareprojekt.SensorType;

/**
 * This exception is thrown when a sensor could not be instantiated
 */
public class SensorNotFoundException extends Exception {
    /**
     * The sensor type that could not be found
     */
    private final SensorType mMissingSensor;

    /**
     * Instantiate a new SensorNotFoundException
     *
     * @param message       more information about the cause etc. of the exception
     * @param missingSensor the sensor that was not found
     */
    SensorNotFoundException(String message, SensorType missingSensor) {
        super(message);
        mMissingSensor = missingSensor;
    }

    public SensorType getMissingSensor(){
        return mMissingSensor;
    }
}
