package de.ovgu.softwareprojekt.tests.pipeline.filters;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.pipeline.filters.AveragingFilter;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test whether the averaging filter behaves as expected
 */
class AveragingFilterTest {
    @org.junit.jupiter.api.Test
    void testAverageCorrectness(){
        AveragingFilter filter = new AveragingFilter(3);

        // check whether the initial conditions are ok
        filter.setDataSink(new AssertionDataSink(new float[]{0, 0, 0}));
        filter.onData(null, new SensorData(null, new float[]{0, 0, 0}, 0), 0);

        // check whether the filter correctly averages the three values
        filter.setDataSink(new AssertionDataSink(new float[]{-1, 2, 3}));
        filter.onData(null, new SensorData(null, new float[]{-3, 6, 9}, 0), 0);

        filter.setDataSink(new AssertionDataSink(new float[]{-2, 4, 6}));
        filter.onData(null, new SensorData(null, new float[]{-3, 6, 9}, 0), 0);

        filter.setDataSink(new AssertionDataSink(new float[]{-3, 6, 0}));
        filter.onData(null, new SensorData(null, new float[]{-3, 6, -18}, 0), 0);
    }
}