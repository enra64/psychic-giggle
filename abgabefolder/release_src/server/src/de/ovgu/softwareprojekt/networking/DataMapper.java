package de.ovgu.softwareprojekt.networking;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
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

    /**
     * Set the ClientConnectionManager notified of potentially required sensor requirement changes
     *
     * @param connectionHandler the ClientConnectionManager notified of potentially required sensor requirement changes
     */
    void setConnectionHandler(ClientConnectionManager connectionHandler) {
        mConnectionHandler = connectionHandler;
    }

    /**
     * Stores all known data sinks. Since they are mapped to their sensor, this also stores the currently required sensors as its keyset.
     * <p>
     * This variable is an EnumMap, so iterations over the keys should be quite fast. The sinks are stored in a
     * HashSet for each sensor, so no sink can occur twice for one sensor type.
     */
    private EnumMap<SensorType, HashSet<SinkOriginFilter>> mDataSinks = new EnumMap<>(SensorType.class);

    /**
     * Register a new data sink to be included in the data stream. Only data from a single client and sensor will arrive.
     *
     * @param dataSink   where new data from the sensor should go
     * @param dataOrigin the network device which is allowed to send to this sink, or null, if any devices data shall be accepted
     * @param sensorType which sensor should be activated, and reach the given NetworkDataSink
     * @throws IOException if a client could not be notified of the sensor change
     */
    void registerDataSink(SensorType sensorType, @Nullable NetworkDevice dataOrigin, NetworkDataSink dataSink) throws IOException {
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

    /**
     * Register a new data sink to be included in the data stream. Only data from a single sensor will arrive.
     *
     * @param dataSink   where new data from the sensor should go
     * @param sensorType which sensor should be activated, and reach the given NetworkDataSink
     * @throws IOException if a client could not be notified of the sensor change
     */
    void registerDataSink(SensorType sensorType, NetworkDataSink dataSink) throws IOException {
        registerDataSink(sensorType, null, dataSink);
    }

    /**
     * Called to notify of the loss of a client. That client will be removed from internal lists.
     *
     * @param client the lost client
     * @throws IOException if another client could not be notified of changed sensor requirements
     */
    void onClientRemoved(NetworkDevice client) throws IOException {
        // remove all instances of the given client
        removeIf(filter -> client.equals(filter.mOrigin), null);
    }

    /**
     * Removes a NetworkDataSink from this data mapper, so that it no longer receives data
     *
     * @param dataSink the data sink that should no longer be used
     * @throws IOException if another client could not be notified of changed sensor requirements
     */
    void unregisterDataSink(NetworkDataSink dataSink) throws IOException {
        // remove all instances of the given data sink
        removeIf(filter -> dataSink.equals(filter.mSink), null);
    }

    /**
     * Unregister a data sink from a single sensor
     *
     * @param dataSink which data sink to remove
     * @param sensor   the sensor for which the data sink was registered
     */
    void unregisterDataSink(NetworkDataSink dataSink, SensorType sensor) throws IOException {
        // remove all instances of the given data sink
        removeIf(filter -> dataSink.equals(filter.mSink), sensor);
    }

    /**
     * Remove all SinkOriginFilters where the given lambda returns true.
     *
     * @param sensor    a {@link SensorType} if only sinks registered for that type should be removed, or null, if all sinks
     *                  where the predicate returns true should be removed
     * @param predicate returns true for all SinkOriginFilters that should be removed
     * @throws IOException if a client could not be notified of updated sensor requirements
     */
    private void removeIf(Predicate<SinkOriginFilter> predicate, @Nullable SensorType sensor) throws IOException {
        // remove all instances of the given network device
        Iterator<Map.Entry<SensorType, HashSet<SinkOriginFilter>>> sensorIterator = mDataSinks.entrySet().iterator();
        while (sensorIterator.hasNext()) {
            Map.Entry<SensorType, HashSet<SinkOriginFilter>> pair = sensorIterator.next();

            // remove all forwarding entries that used this client
            if (sensor == null || sensor == pair.getKey())
                pair.getValue().removeIf(predicate);

            // remove the sensor -> hashSet mapping if there are no longer any sinks registered for the sensor
            if (pair.getValue().size() == 0)
                sensorIterator.remove();
        }

        // force resetting the sensors on the client
        mConnectionHandler.updateSensors(mDataSinks.keySet());
    }

    /**
     * onData is called whenever new data is to be processed. here, we forwardData the data to
     *
     * @param origin          the network device which sent the data
     * @param data            the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        HashSet<SinkOriginFilter> registeredSinks = mDataSinks.get(data.sensorType);

        for (NetworkDataSink sink : registeredSinks)
            sink.onData(origin, data, userSensitivity);
    }

    /**
     * called when the sink is no longer used and should no longer require resources. no commands required here
     */
    @Override
    public void close() {
    }


    /**
     * This is a small NetworkDataSink that only forwards data from one origin if the
     * {@link SinkOriginFilter#SinkOriginFilter(NetworkDataSink, NetworkDevice)} constructor is used, or all data if the
     * NetworkDevice parameter is null. It allows us to register data sinks for data coming only from a
     * specified {@link SensorType} and {@link NetworkDevice}.
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
         * Create a new SinkOriginFilter that will only forwardData data stemming from origin
         *
         * @param sink   where to put data
         * @param origin what origin device should be forwarded
         */
        SinkOriginFilter(NetworkDataSink sink, @Nullable NetworkDevice origin) {
            mSink = sink;
            mOrigin = origin;
        }

        /**
         * Forwards data if no specific origin is required, or the data is from the correct client
         *
         * @param origin          the network device which sent the data
         * @param data            the sensor data
         * @param userSensitivity the sensitivity the user requested in his app settings for the sensor
         *                        the data is from
         */
        @Override
        public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
            // only forwardData the data if all-forwardData is enabled (origin null) or the data is from the correct device
            if (mOrigin == null || mOrigin == origin)
                mSink.onData(origin, data, userSensitivity);
        }

        /**
         * No implementation necessary
         */
        @Override
        public void close() {
        }

        /**
         * hashCode implementation that identifies two DataMapper instances as the same if the sink and origin is the same
         *
         * @return a hashcode as specified above
         */
        @Override
        public int hashCode() {
            return mSink.hashCode() + (mOrigin == null ? 0 : mOrigin.hashCode());
        }
    }
}
