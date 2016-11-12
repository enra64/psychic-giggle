package de.ovgu.softwareprojekt;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class encapsulates all data for a single sensor event
 */
@SuppressWarnings("WeakerAccess")
public class SensorData implements Serializable, Cloneable {
    /**
     * Type of the sensor this data belongs to
     */
    public SensorType sensorType = SensorType.Invalid;

    /**
     * Data contained in this instance
     */
    public float[] data = null;

    /**
     * Timestamp
     * TODO: unit and reference should still be decided
     */
    public long timestamp = 0;

    /**
     * Accuracy of the sensor values
     * TODO: do we even care about this
     */
    public int accuracy = -1;

    /**
     * Create a new SensorType instance
     */
    public SensorData(SensorType sensorType, float[] data, long timestamp, int accuracy) {
        this.sensorType = sensorType;
        this.data = data;
        this.timestamp = timestamp;
        this.accuracy = accuracy;
    }

    /**
     * Create an empty SensorData instance
     */
    public SensorData() {
    }

    /**
     * Create a csv representation of this object
     */
    @Override
    public String toString() {
        // print the data as csv
        return Arrays.toString(data).replaceAll("[ \\[\\]]", "") + "," + timestamp + "," + accuracy;
    }

    /**
     * Return a properly cloned SensorData object
     */
    @Override
    public SensorData clone() {
        // declare result
        SensorData cloned = null;

        // try { clone() } to avoid "throws" in signature
        try {
            // have to call super.clone() ensure proper initialization
            cloned = (SensorData) super.clone();
            cloned.sensorType = sensorType;
            cloned.data = data.clone();
            cloned.timestamp = timestamp;
            cloned.accuracy = accuracy;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // this must not be reached, as we inherit directly from Object
        assert(false);
        return cloned;
    }
}
