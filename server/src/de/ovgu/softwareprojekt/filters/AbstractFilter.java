package de.ovgu.softwareprojekt.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.NetworkDataSource;

import java.io.IOException;

/**
 * Created by Ulrich on 29.11.2016.
 * This abstract filter implements a lot of the interface methods used by the inherited classes and
 * provides the DataSink and axes
 */
public abstract class AbstractFilter implements NetworkDataSink, NetworkDataSource {

    protected final int XAXIS, YAXIS, ZAXIS;
    protected NetworkDataSink mDataSink;

    public AbstractFilter(NetworkDataSink sink, int xaxis, int yaxis, int zaxis) {
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
    public void setDataSink(NetworkDataSink dataSink) {
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
