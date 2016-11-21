package de.ovgu.softwareprojekt;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;

/**
 * This class encapsulates all data for a single sensor event
 */
@SuppressWarnings("WeakerAccess")
public class SensorData implements Serializable, Cloneable, Externalizable {
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
     */
    public long timestamp = 0;

    /**
     * Create a new SensorType instance
     */
    public SensorData(SensorType sensorType, float[] data, long timestamp) {
        this.sensorType = sensorType;
        this.data = data;
        this.timestamp = timestamp;
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
        return Arrays.toString(data).replaceAll("[ \\[\\]]", "") + "," + timestamp;
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
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // this must not be reached, as we inherit directly from Object
        return cloned;
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        // write sensor type
        objectOutput.writeUTF(sensorType.name());

        // writes data length and then data
        objectOutput.writeInt(data.length);
        for(float f : data)
            objectOutput.writeFloat(f);

        objectOutput.writeLong(timestamp);
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        sensorType = SensorType.valueOf(objectInput.readUTF());

        // create data array
        int dataCount = objectInput.readInt();
        data = new float[dataCount];

        // read back data
        for(int i = 0; i < dataCount; i++)
            data[i] = objectInput.readFloat();

        timestamp = objectInput.readLong();
    }
}
