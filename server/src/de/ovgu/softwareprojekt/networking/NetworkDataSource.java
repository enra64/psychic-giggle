package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.*;

import java.io.IOException;

/**
 * Interface for sensor data sources on the server side. This is implemented by {@link de.ovgu.softwareprojekt.pipeline.filters.AbstractFilter}
 * and the DataConnection. You do not normally need to implement this directly.
 * <p>
 * On the app side, the origin and the sensor sensitivity of data are not relevant, so {@link DataSource} is used there.
 */
public interface NetworkDataSource {
    /**
     * Save the data sink to be used for pushing new data
     *
     * @param sink any implementation of the {@link de.ovgu.softwareprojekt.NetworkDataSink} interface
     */
    void setDataSink(NetworkDataSink sink);

    /**
     * Start sending data
     */
    void start() throws IOException;

    /**
     * called when the source is no longer used and should no longer consume resources
     */
    void close();
}
