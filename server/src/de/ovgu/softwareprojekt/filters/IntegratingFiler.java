package de.ovgu.softwareprojekt.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

/**
 * Created by Ulrich on 12.12.2016.
 *
 * This Filter is responsible for saving previous values so that for example
 * the steering rotation is used instead of acting as if the steering wheel is back at 0
 * TODO: better description and not this whackshit
 */
public class IntegratingFiler extends AbstractFilter {

    private float mSumOfRotationX, mSumOfRotationY, mSumOfRotationZ;

    public IntegratingFiler(NetworkDataSink sink) {
        super(sink, 0, 1, 2);
        mSumOfRotationX = 0f;
        mSumOfRotationY = 0f;
        mSumOfRotationZ = 0f;
    }

    /**
     * Add current Values to the AXES_SUM and save them in the pipeline
     * @param data current values
     */
    @Override
    public void onData(NetworkDevice origin, SensorData data) {
        mSumOfRotationX += data.data[XAXIS];
        mSumOfRotationY += data.data[YAXIS];
        mSumOfRotationZ += data.data[ZAXIS];

        data.data[XAXIS] = mSumOfRotationX;
        data.data[YAXIS] = mSumOfRotationY;
        data.data[ZAXIS] = mSumOfRotationZ;

        mDataSink.onData(origin, data);
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
