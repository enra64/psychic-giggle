package de.ovgu.softwareprojektapp.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.IOException;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.DataSource;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.SetSensorSpeed;

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
     *
     * @param context    android system context needed for sensors
     * @param sensorType sensor type we should register for
     */
    public AbstractSensor(Context context, int sensorType, SensorType psychicSensorType) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorType = sensorType;
        mPsychicSensorType = psychicSensorType;
    }

    /**
     * Set the data sink that is to be notified of events on the sensor
     *
     * @param sink any implementation of the {@link DataSink} interface
     */
    @Override
    public void setDataSink(DataSink sink) {
        mSink = sink;
    }

    /**
     * Shorthand for {@link #start} and {@link #close()}.
     *
     * @param enable true if the sensor output should be started
     */
    void setRunning(boolean enable) throws IOException {
        if (enable)
            start();
        else
            close();
    }

    /**
     * Registers a listener for sensor events; will start sending data to the registered sink
     */
    @Override
    public void start() throws IOException {
        // abort if already registered or no sensor manager
        if (mListenerRegistered || mSensorManager == null)
            return;

        // keep track of registration state
        mListenerRegistered = true;

        // register for sensor events
        Sensor sensor = mSensorManager.getDefaultSensor(mSensorType);
        //check if sensor exist on this device
        if (sensor == null)
            throw new IOException("Sensor not detected");
        mSensorManager.registerListener(this, sensor, translateSensorSpeed(getSensorSpeed()));
    }

    /**
     * Translate the sensor speed from {@link de.ovgu.softwareprojekt.control.commands.SetSensorSpeed.SensorSpeed}
     * to Androids integer constants
     *
     * @param sensorSpeed the sensor speed that should be translated
     * @return an int representing the given sensor speed, like SensorManager.SENSOR_DELAY_WHATEVER
     */
    private int translateSensorSpeed(SetSensorSpeed.SensorSpeed sensorSpeed) {
        switch (sensorSpeed) {
            case SENSOR_DELAY_FASTEST:
                return SensorManager.SENSOR_DELAY_FASTEST;
            case SENSOR_DELAY_GAME:
                return SensorManager.SENSOR_DELAY_GAME;
            case SENSOR_DELAY_NORMAL:
                return SensorManager.SENSOR_DELAY_NORMAL;
            // the default case should not happen. it is mostly for shutting up the compiler.
            default:
            case SENSOR_DELAY_UI:
                return SensorManager.SENSOR_DELAY_UI;
        }
    }

    /**
     * Changes the frequency of this sensor. Because this has to re-register the listener,
     * a slight delay might be experienced. The sensor speed variable should be static, so
     * that setting the sensor speed in one instance sets it for all instances of the given
     * sensor.
     *
     * @param targetSpeed the speed the sensor should run at after this call
     * @throws IOException when the sensor could not be restarted
     */
    public abstract void setSensorSpeed(SetSensorSpeed.SensorSpeed targetSpeed) throws IOException;

    /**
     * This function must give the current sensor speed set for the sensor. As the sensor speed
     * must be static, the sensor speed should be the same for all sensor instances
     *
     * @return static sensor speed for the sensor.
     */
    public abstract SetSensorSpeed.SensorSpeed getSensorSpeed();

    /**
     * Unregister sensor listener
     */
    @Override
    public void close() {
        // abort if not registered yet
        if (!mListenerRegistered || mSensorManager == null)
            return;

        // unregister the listener
        mSensorManager.unregisterListener(this);

        // keep track of registration state
        mListenerRegistered = false;
    }

    /**
     * Get the state of listener registration
     *
     * @return true if the listener is currently registered
     */
    public boolean isRegistered() {
        return mListenerRegistered;
    }

    /**
     * Returns the sensor type the implementation deals with
     */
    SensorType getSensorType() {
        return mPsychicSensorType;
    }

    /**
     * Called whenever a new sensorEvent is available
     *
     * @param sensorEvent new sensor data
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // if no sink is set yet, discard the SensorEvent
        if (mSink == null)
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
