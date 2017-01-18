package de.ovgu.softwareprojekt.servers.kuka;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Created by arne on 18.01.17.
 */
public class JointControl implements NetworkDataSink {
    /**
     * onData is called whenever new data is to be processed
     *
     * @param origin          the network device which sent the data
     * @param data            the sensor data
     * @param userSensitivity the sensitivity the user requested in his app settings
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {

    }

    /**
     * called when the sink is no longer used and should no longer require resources
     */
    @Override
    public void close() {

    }
}
