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
    /**
     * TODO some axis swapping thing
     */
    protected final int XAXIS, YAXIS, ZAXIS;

    /**
     * The data sink all data processed by this filter will be dumped into
     */
    protected NetworkDataSink mDataSink;

    /**
     *
     * @param sink either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *             must be called prior to starting operations.
     * @param xaxis // TODO see AbstractFilter.java:52
     * @param yaxis
     * @param zaxis
     */
    public AbstractFilter(@Nullable NetworkDataSink sink, int xaxis, int yaxis, int zaxis) {
        this(xaxis, yaxis, zaxis);
        mDataSink = sink;
    }

    /**
     * Create a new {@link AbstractFilter} with the default axes.
     * @param sink either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *             must be called prior to starting operations.
     */
    public AbstractFilter(@Nullable NetworkDataSink sink) {
        this();
        mDataSink = sink;
    }

    /**
     * Create a new {@link AbstractFilter} with the default axes
     */
    public AbstractFilter(){
        this(0, 1, 2);
    }

    /**
     * Create a new abstract filter with specified axes
     * @param xaxis //TODO we still have no idea what these axes mean. do the child classes need to
     *                TODO properly implement this to work?? could we possibly override onData to swap
     *                TODO the values before giving them to child classes? is that even possible?
     *                TODO maybe some kind of forced super() call in the onData?
     * @param yaxis //TODO do not remove this todo until we have a proper description
     * @param zaxis //TODO team "uuuh we have no todos left" im looking at you
     */
    public AbstractFilter(int xaxis, int yaxis, int zaxis){
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
