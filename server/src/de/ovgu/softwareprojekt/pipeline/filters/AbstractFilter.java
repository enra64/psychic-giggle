package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.NetworkDataSource;

import java.io.IOException;

/**
 * Created by Ulrich on 29.11.2016.
 * This abstract filter implements a lot of the interface methods used by the inherited classes and
 * provides the DataSink and axes
 */
public abstract class AbstractFilter implements NetworkDataSink, NetworkDataSource {
    // constants are the standard ids from android sensors/sensor data
    protected final int XAXIS = 0, YAXIS = 1, ZAXIS =2;

    /**
     * The data sink all data processed by this filter will be dumped into
     */
    protected NetworkDataSink mDataSink;

    /**
     * Create a new {@link AbstractFilter}.
     * @param sink either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *             must be called prior to starting operations.
     */
    public AbstractFilter(@Nullable NetworkDataSink sink) {
        mDataSink = sink;
    }

    /**
     * Create a new {@link AbstractFilter} with a null DataSink
     */
    protected AbstractFilter() {
        this(null);
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
