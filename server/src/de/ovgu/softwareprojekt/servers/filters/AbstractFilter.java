package de.ovgu.softwareprojekt.servers.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorData;

import java.io.IOException;

/**
 * Created by Ulrich on 29.11.2016.
 */
public abstract class AbstractFilter implements DataSink, DataSource {

    protected final int XAXIS, YAXIS, ZAXIS;
    protected DataSink mDataSink;

    public AbstractFilter(DataSink sink, int xaxis, int yaxis, int zaxis) {
        mDataSink = sink;
        XAXIS = xaxis;
        YAXIS = yaxis;
        ZAXIS = zaxis;
    }

    /**
     * Save the data sink to be used for pushing new data
     *
     * @param dataSink any implementation of the {@link DataSink} interface
     */
    @Override
    public void setDataSink(DataSink dataSink) {
        mDataSink = dataSink;
    }

    /**
     * Interface requirement; does not do anything for this implementation.
     *
     * @throws IOException never
     */
    @Override
    public void start() throws IOException {
    }

    /**
     * Interface requirement. Does not do anything in this implementation.
     */
    @Override
    public void close() {
    }

}
