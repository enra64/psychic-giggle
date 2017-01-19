package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is designed to forward data to NetworkDataSinks.
 * <p>
 * It supports mapping them from (SensorType, NetworkDevice) tuples or from SensorType to NetworkDataSink.
 */
class DataMapper implements NetworkDataSink {
    /**
     * This class is used for keeping the sensor-, button and speed requirements synchronous
     */
    private ClientConnectionManager mConnectionHandler;

    void setConnectionHandler(ClientConnectionManager connectionHandler){
        mConnectionHandler = connectionHandler;
    }

    /**
     * Stores all known data sinks
     * <p>
     * This variable is an EnumMap, so iterations over the keys should be quite fast. The sinks are stored in a
     * HashSet for each sensor, so no sink can occur twice for one sensor type.
     */
    private EnumMap<SensorType, HashSet<SinkOriginFilter>> mDataSinks = new EnumMap<>(SensorType.class);

    public void registerDataSink(SensorType sensorType, @Nullable NetworkDevice dataOrigin, NetworkDataSink dataSink) throws IOException {
        // dataSink *must not* be this, as that would lead to infinite recursion
        if (this == dataSink)
            throw new InvalidParameterException("DataMapper must not register itself as data sink to avoid infinite recursion.");

        // add new data sink list if the requested sensor type has no sinks yet
        if (!mDataSinks.containsKey(sensorType))
            mDataSinks.put(sensorType, new HashSet<>());

        // add the new sink to the list of sinks for the sensor
        mDataSinks.get(sensorType).add(new SinkOriginFilter(dataSink, dataOrigin));

        // force resetting the sensors on the client
        mConnectionHandler.updateSensors(mDataSinks.keySet());
    }

    void registerDataSink(SensorType sensorType, NetworkDataSink dataSink) throws IOException {
        registerDataSink(sensorType, null, dataSink);
    }

    void onClientRemoved(NetworkDevice client) {
        // remove all instances of the given client
        removeIf(filter -> client.equals(filter.mOrigin), null);
    }

    void unregisterDataSink(NetworkDataSink dataSink){
        // remove all instances of the given data sink
        removeIf(filter -> dataSink.equals(filter.mSink), null);
    }

    public void unregisterDataSink(NetworkDataSink dataSink, SensorType sensor) {
        // remove all instances of the given data sink
        removeIf(filter -> dataSink.equals(filter.mSink), sensor);
    }

    /**
     * Remove all SinkOriginFilters where the given lambda rings true
     * @param predicate returns true for all SinkOriginFilters that should be removed
     */
    private void removeIf(Predicate<SinkOriginFilter> predicate, SensorType sensor){
        // remove all instances of the given network device
        Iterator<Map.Entry<SensorType, HashSet<SinkOriginFilter>>> sensorIterator = mDataSinks.entrySet().iterator();
        while (sensorIterator.hasNext()) {
            Map.Entry<SensorType, HashSet<SinkOriginFilter>> pair = sensorIterator.next();

            // remove all forwarding entries that used this client
            if(sensor == null || sensor == pair.getKey())
                pair.getValue().removeIf(predicate);

            // remove the sensor -> hashSet mapping if there are no longer any sinks registered for the sensor
            if(pair.getValue().size() == 0)
                sensorIterator.remove();
        }
    }

    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        HashSet<SinkOriginFilter> registeredSinks = mDataSinks.get(data.sensorType);

        for (NetworkDataSink sink : registeredSinks)
            sink.onData(origin, data, userSensitivity);
    }

    @Override
    public void close() {
    }


    /**
     * This is a small NetworkDataSink that only forwards data from one origin if the
     * {@link #SinkOriginFilter(NetworkDataSink, NetworkDevice)} constructor is used, or all data if the
     * NetworkDevice parameter is null.
     */
    private class SinkOriginFilter implements NetworkDataSink {
        /**
         * The sink that should receive any data received by this filter
         */
        private final NetworkDataSink mSink;

        /**
         * The network device whose data is accepted, or null if any data should be forwarded
         */
        private final NetworkDevice mOrigin;

        /**
         * Create a new SinkOriginFilter that will only forward data stemming from origin
         * @param sink where to put data
         * @param origin what origin device should be forwarded
         */
        SinkOriginFilter(NetworkDataSink sink, NetworkDevice origin) {
            mSink = sink;
            mOrigin = origin;
        }

        @Override
        public void onData(NetworkDevice networkDevice, SensorData sensorData, float v) {
            // only forward the data if all-forward is enabled (origin null) or the data is from the correct device
            if (mOrigin == null || mOrigin == networkDevice)
                mSink.onData(networkDevice, sensorData, v);
        }

        @Override
        public void close() {
        }

        @Override
        public int hashCode() {
            return mSink.hashCode() + (mOrigin == null ? 0 : mOrigin.hashCode());
        }
    }
}
