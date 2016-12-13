package de.ovgu.softwareprojekt.filters;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorData;

/**
 * Created by Ulrich on 12.12.2016.
 *
 * This Filter is responsible for saving previous values so that for example
 * the steering rotation is used instead of acting as if the steering wheel is back at 0
 * TODO: better description and not this whackshit
 */
public class IntegratingFiler extends AbstractFilter {

    private float mSumOfRotationX, mSumOfRotationY, mSumOfRotationZ;

    public IntegratingFiler(DataSink sink) {
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
    public void onData(SensorData data) {
        mSumOfRotationX += data.data[XAXIS];
        mSumOfRotationY += data.data[YAXIS];
        mSumOfRotationZ += data.data[ZAXIS];

        data.data[XAXIS] = mSumOfRotationX;
        data.data[YAXIS] = mSumOfRotationY;
        data.data[ZAXIS] = mSumOfRotationZ;

        mDataSink.onData(data);
    }

}
