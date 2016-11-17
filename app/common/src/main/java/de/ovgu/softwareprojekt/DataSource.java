package de.ovgu.softwareprojekt;

import java.io.IOException;

/**
 * Interface for sensor data sources
 */
public interface DataSource {

    /**
     * Save the data sink to be used for pushing new data
     * @param sink any implementation of the {@link DataSink} interface
     */
    void setDataSink(DataSink sink);

    /**
     * Start sending data
     */
    void start() throws IOException;

    /**
     * called when the source is no longer used
     */
    void close();
}
