package de.ovgu.softwareprojekt.tests.pipeline.filters;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AssertionDataSink implements NetworkDataSink {
    private float[] mExpected;

    public AssertionDataSink(float[] expected) {
        mExpected = expected;
    }

    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData, float v) {
        assertTrue(Arrays.equals(sensorData.data, mExpected), "The incoming data must match the expected.");
    }

    @Override
    public void close() {
    }
}