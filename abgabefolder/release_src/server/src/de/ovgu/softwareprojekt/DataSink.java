package de.ovgu.softwareprojekt;

/**
 * Interface for sensor data sinks
 */
@SuppressWarnings("WeakerAccess")
public interface DataSink {

    /**
     * onData is called whenever the sensor has received and processed new data
     */
    void onData(SensorData data);

    /**
     * called when the sink is no longer used
     */
    void close();
}
