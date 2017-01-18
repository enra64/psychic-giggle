package de.ovgu.softwareprojekt.pipeline.splitters;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Created by arne on 18.01.17.
 */
public class Switch implements NetworkDataSink {
    private boolean mForwardToFirstSink;

    private NetworkDataSink mFirstSink, mSecondSink;

    public Switch(NetworkDataSink first, NetworkDataSink secondSink, boolean startWithFirst){
        mFirstSink = first;
        mSecondSink = secondSink;
        mForwardToFirstSink = startWithFirst;
    }

    public void setFirst(NetworkDataSink firstSink) {
        mFirstSink = firstSink;
    }

    public void setSecond(NetworkDataSink secondSink) {
        mSecondSink = secondSink;
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
