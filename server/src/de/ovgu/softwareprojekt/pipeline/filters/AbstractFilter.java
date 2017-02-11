package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.networking.NetworkDataSource;

import java.io.IOException;

/**
 * This abstract filter implements a lot of the interface methods used by the inherited classes and
 * provides the DataSink. Used to create a new filter usable by the data pipeline.
 */
public abstract class AbstractFilter implements NetworkDataSink, NetworkDataSource {
    /**
     * The data sink all data processed by this filter will be dumped into
     */
    private NetworkDataSink mDataSink;

    /**
     * Create a new {@link AbstractFilter}.
     *
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
     * This retrieves the next pipeline element
     *
     * @return the {@link NetworkDataSink} that is due to receive the data next
     */
    protected NetworkDataSink getNextDataSink() {
        return mDataSink;
    }

    /**
     * This forwards the given data to the next pipeline element. It is a convenience function that calls
     * {@link NetworkDataSink#onData(NetworkDevice, SensorData, float)} on the next pipeline element.
     * <p>
     * Be aware that this function does *nothing* if the next pipeline element is <code>null</code>. It can be set using either
     * {@link AbstractFilter#AbstractFilter(NetworkDataSink)} or {@link #setDataSink(NetworkDataSink)}.
     *
     * @param origin      the network device which sent the data
     * @param data        the sensor data
     * @param sensitivity 0-100 inclusive. unless you consciously want to modify the user sensitivity (for example, because
     *                    you modify the data to mirror the sensitivity setting) this should just forward the sensitivity parameter.
     * @return true if a next pipeline element was found.
     */
    protected boolean forwardData(NetworkDevice origin, SensorData data, float sensitivity) {
        // abort if next element is not set
        if (!hasNextPipelineElement())
            return false;

        // forward data
        mDataSink.onData(origin, data, sensitivity);
        return true;
    }

    /**
     * Whether this filter element has a next pipeline element set using either
     * {@link AbstractFilter#AbstractFilter(NetworkDataSink)} or {@link #setDataSink(NetworkDataSink)}.
     *
     * @return true if the next pipeline element is not <code>null</code>
     */
    public boolean hasNextPipelineElement() {
        return mDataSink != null;
    }

    /**
     * Interface requirement. Does not do anything in this implementation.
     */
    @Override
    public void close() {
    }

}
