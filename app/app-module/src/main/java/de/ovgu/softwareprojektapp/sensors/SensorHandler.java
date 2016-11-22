package de.ovgu.softwareprojektapp.sensors;

import android.content.Context;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorType;

/**
 * Handle instantiation and storage of sensor classes
 */
public class SensorHandler {
    /**
     * This map stores already instantiated sensor classes
     */
    private EnumMap<SensorType, AbstractSensor> mSensors = new EnumMap<>(SensorType.class);

    /**
     * Context stored for android interaction purposes
     */
    private Context mContext;

    /**
     * Create a new SensorHandler
     *
     * @param context required for interaction with android sensor system
     */
    public SensorHandler(Context context) {
        mContext = context;
    }

    private HashSet<SensorType> mTemporarilyDisabledSensors = null;


    @SuppressWarnings("unchecked")
    public boolean setRunning(DataSink dataSink, List<SensorType> requiredSensors) {
        boolean success = true;

        // enable all required sensors
        for (SensorType requiredSensor : requiredSensors)
            success = setSensor(dataSink, requiredSensor, true) && success;

        // get list of unnecessary sensors
        Set<SensorType> notRequiredSensors = new HashSet<>(mSensors.keySet());
        notRequiredSensors.removeAll(requiredSensors);

        // disable all unnecessary sensors
        for (SensorType notRequiredSensor : notRequiredSensors)
            success = setSensor(dataSink, notRequiredSensor, false) && success;

        return success;
    }

    /**
     * This method tries to dis- or enable a certain sensor.
     *
     * @param dataSink   the datasink the sensor should report to
     * @param sensorType the list of required sensors
     * @return true if the sensor state has been successfully set
     */
    private boolean setSensor(DataSink dataSink, SensorType sensorType, boolean enable) {
        try {
            // if we must enable the sensor, check whether it exists beforehand
            if (enable) {
                // already exists, just set running and update data sink
                if (mSensors.containsKey(sensorType)) {
                    mSensors.get(sensorType).setRunning(true);
                    mSensors.get(sensorType).setDataSink(dataSink);
                }
                // create new sensor
                else
                    instantiateSensor(sensorType, dataSink);
            }
            // disable the sensor
            else {
                if (mSensors.containsKey(sensorType)) {
                    mSensors.get(sensorType).setRunning(false);
                    mSensors.remove(sensorType);
                }
            }
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException | IOException e) {
            return false;
        }
        return true;
    }

    public void enableTemporarilyDisabledSensors() throws IOException {
        // enable all sensors that were in our list of required sensors
        if (mTemporarilyDisabledSensors != null) {
            for (SensorType sensor : mTemporarilyDisabledSensors)
                mSensors.get(sensor).setRunning(true);
            mTemporarilyDisabledSensors = null;
        }
    }

    public void temporarilyDisableSensors() throws IOException {
        mTemporarilyDisabledSensors = new HashSet<>(mSensors.keySet());

        // disable all sensors that were in our list of required sensors
        for (SensorType sensor : mTemporarilyDisabledSensors)
            mSensors.get(sensor).setRunning(false);
    }

    /**
     * Instantiate a sensor
     *
     * @param sensorType the sensor type to be initiated
     * @param dataSink   where the sensor should send its data
     * @throws NoSuchMethodException     if the sensor could not be instantiated
     * @throws IllegalAccessException    if the sensor could not be instantiated
     * @throws InvocationTargetException if the sensor could not be instantiated
     * @throws InstantiationException    if the sensor could not be instantiated
     * @throws IOException               if the sensor could not be started
     */
    @SuppressWarnings("unchecked")
    private void instantiateSensor(SensorType sensorType, DataSink dataSink) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        Class sensorImplementation = getClassForSensorType(sensorType);

        // find the constructor for this class
        Constructor<?> constructor = sensorImplementation.getConstructor(Context.class);

        // instantiate it
        AbstractSensor instance = (AbstractSensor) constructor.newInstance(mContext);

        // configure
        instance.setRunning(true);
        instance.setDataSink(dataSink);

        // put into sensor list to avoid next instantiation
        mSensors.put(sensorType, instance);
    }

    /**
     * Close all sensors (unregister them)
     */
    public void closeAll() {
        for (AbstractSensor abstractSensor : mSensors.values())
            abstractSensor.close();
    }

    /**
     * Retrieves the class necessary to create sensor data for a certain sensor type
     *
     * @param sensorType the required sensor type
     * @return the class usable for creating such sensor data
     * @throws NoSuchMethodException if no fitting AbstractSensor implementation is known
     */
    private Class getClassForSensorType(SensorType sensorType) throws NoSuchMethodException {
        switch (sensorType) {
            case Accelerometer:
                return Accelerometer.class;
            case Gyroscope:
                return Gyroscope.class;
            default:
                throw new NoSuchMethodException();
        }
    }
}
