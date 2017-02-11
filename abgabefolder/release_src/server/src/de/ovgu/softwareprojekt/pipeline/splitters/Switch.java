package de.ovgu.softwareprojekt.pipeline.splitters;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * This pipeline splitter forwards incoming data to one of two data sinks.
 */
@SuppressWarnings("WeakerAccess")
public class Switch implements NetworkDataSink {
    /**
     * True if the data should be forwarded to {@link #mFirstSink}, false if it should be forwarded
     * to {@link #mSecondSink}.
     */
    private boolean mForwardToFirstSink;

    /**
     * Possible target destination for incoming data. Chosen using {@link #mForwardToFirstSink}
     */
    private NetworkDataSink mFirstSink, mSecondSink;

    /**
     * Create a new pipeline switch
     * @param firstSink the sink that will get data if {@link #routeToFirst()} is called
     * @param secondSink the sink that will get data if {@link #routeToSecond()} is called
     * @param startWithFirst true if the switch should send all data to firstSink until otherwise requested
     */
    public Switch(NetworkDataSink firstSink, NetworkDataSink secondSink, boolean startWithFirst){
        mFirstSink = firstSink;
        mSecondSink = secondSink;
        mForwardToFirstSink = startWithFirst;
    }

    /**
     * Change the data sink that is regarded to as first
     * @param firstSink new first sink
     */
    public void setFirst(NetworkDataSink firstSink) {
        mFirstSink = firstSink;
    }

    /**
     * Change the data sink that is regarded to as second
     * @param secondSink new first sink
     */
    public void setSecond(NetworkDataSink secondSink) {
        mSecondSink = secondSink;
    }

    /**
     * Change routing destination
     * @param routeToFirst true if first sink should get data
     */
    public void routeToFirst(boolean routeToFirst){
        mForwardToFirstSink = routeToFirst;
    }

    /**
     * Change routing destination to first sink
     */
    public void routeToFirst() {
        routeToFirst(true);
    }

    /**
     * Change routing destination to second sink
     */
    public void routeToSecond() {
        routeToFirst(false);
    }

    /**
     * onData is called whenever new data is to be processed
     *
     * @param origin          the network device which sent the data
     * @param data            the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        if(mForwardToFirstSink)
            mFirstSink.onData(origin, data, userSensitivity);
        else
            mSecondSink.onData(origin, data, userSensitivity);
    }

    /**
     * called when the sink is no longer used and should no longer require resources
     */
    @Override
    public void close() {
    }
}
