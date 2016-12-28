package de.ovgu.softwareprojekt.pipeline.filters;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Created by Ulrich on 12.12.2016.
 *
 * This Filter is responsible for saving previous values so that for example
 * the steering rotation is used instead of acting as if the steering wheel is back at 0
 * TODO: better description and not this whackshit lul
 */
public class IntegratingFilter extends AbstractFilter {
    /**
     * Theses are storage variables, where the sum of al previous values is stored
     */
    private float mSumOfRotationX = 0, mSumOfRotationY = 0, mSumOfRotationZ = 0;

    /**
     * Create a new {@link IntegratingFilter}
     * @param sink either a valid network data sink, or null. if null, {@link #setDataSink(NetworkDataSink)}
     *             must be called prior to starting operations.
     */
    public IntegratingFilter(@Nullable NetworkDataSink sink) {
        super(sink);
    }

    /**
     * Add current Values to the AXES_SUM and save them in the pipeline
     * @param data current values
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data, float userSensitivity) {
        mSumOfRotationX += data.data[XAXIS];
        mSumOfRotationY += data.data[YAXIS];
        mSumOfRotationZ += data.data[ZAXIS];

        data.data[XAXIS] = mSumOfRotationX;
        data.data[YAXIS] = mSumOfRotationY;
        data.data[ZAXIS] = mSumOfRotationZ;

        mDataSink.onData(origin, data, userSensitivity);
    }

    /**
     * This method sets all axes sums back to 0 in order to set a new initial position for the smart phone
     */
    public void resetFilter(){
        mSumOfRotationX = 0;
        mSumOfRotationY = 0;
        mSumOfRotationZ = 0;
    }

}
