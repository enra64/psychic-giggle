package de.ovgu.softwareprojektapp.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.IOException;
import java.security.InvalidParameterException;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;

/**
 * Super class for our sensors as much of the work is the same for any sensor type
 */
public abstract class AbstractSensor implements DataSource, SensorEventListener {
    /**
     * The android sensor manager used to connect to the accelerometer
     */
    private final SensorManager mSensorManager;

    /**
     * The sink data should be pushed into
     */
    private DataSink mSink;

    /**
     * True if we already have registered as a sensor listener
     */
    private boolean mListenerRegistered = false;

    /**
     * What sensor type this sensor should listen to
     */
    private final int mSensorType;

    /**
     * Since we have our own definition of sensor type (android definitions not available in common),
     * we want to save that, too
     */
    private final SensorType mPsychicSensorType;

    /**
     * Create a new sensor object; does not start anything yet, use {@link #start()} or {@link #setRunning(boolean)}
     * to start receiving events
     * @param context android system context needed for sensors
     * @param sensorType sensor type we should register for
     */
    AbstractSensor(Context context, int sensorType, SensorType psychicSensorType){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorType = sensorType;
        mPsychicSensorType = psychicSensorType;
    }

    /**
     * Retrieve the android sensor manager
     */
    protected SensorManager getSensorManager(){
        return mSensorManager;
    }

    /**
     * Set the data sink that is to be notified of events on the sensor
     * @param sink any implementation of the {@link DataSink} interface
     */
    @Override
    public void setDataSink(DataSink sink) {
        mSink = sink;
    }

    /**
     * Shorthand for {@link #start} and {@link #close()}.
     * @param enable true if the sensor output should be started
     */
    public void setRunning(boolean enable) throws IOException{
        if(enable)
            start();
        else
            close();
    }

    /**
     * Registers a listener for sensor events; will start sending data to the registered sink
     */
    @Override
    public void start() throws IOException{
        // abort if already registered
        if(mListenerRegistered)
            return;

        // keep track of registration state
        mListenerRegistered = true;

        // register for sensor events
        Sensor sensor = mSensorManager.getDefaultSensor(mSensorType);
        //check if sensor exist on this device
        if(sensor == null)
            throw new IOException("Sensor not detected");
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Unregister sensor listener
     */
    @Override
    public void close() {
        // unregister the listener
        mSensorManager.unregisterListener(this);

        // keep track of registration state
        mListenerRegistered = false;
    }

    /**
     * Returns the sensor type the implementation deals with
     */
    public SensorType getSensorType(){
        return mPsychicSensorType;
    }

    /**
     * Called whenever a new sensorEvent is available
     * @param sensorEvent new sensor data
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // if no sink is set yet, discard the SensorEvent
        if(mSink == null)
            return;

        // push data into sink
        mSink.onData(
                new SensorData(
                        SensorType.Gyroscope,
                        sensorEvent.values,
                        sensorEvent.timestamp));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
