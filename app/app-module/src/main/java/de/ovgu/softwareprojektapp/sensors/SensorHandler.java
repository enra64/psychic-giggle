package de.ovgu.softwareprojektapp.sensors;

import android.content.Context;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorType;

/**
 * Handle instantiation and storage of sensor classes, as well as temporarily disabling them.
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

    private DataSink mDataSink;

    /**
     * Create a new SensorHandler
     *
     * @param context  required for interaction with android sensor system
     * @param dataSink required for pushing the data somewhere
     */
    public SensorHandler(Context context, DataSink dataSink) {
        mContext = context;
        mDataSink = dataSink;
    }

    private EnumMap<SensorType, Boolean> mTemporarySensorActivationList = new EnumMap<>(SensorType.class);

    private boolean mSensorsTempDisabled = false;

    @SuppressWarnings("unchecked")
    public boolean setRunning(List<SensorType> requiredSensors) {
        // if the sensors are currently disabled, only update the list of sensors that will be
        // enabled after starting them again
        if (mSensorsTempDisabled) {
            mTemporarySensorActivationList = getSensorActivationList(requiredSensors);
            return true;
        }

        // try to apply the list of required sensors
        return applySensorActivationList(getSensorActivationList(requiredSensors));
    }

    /**
     * Create a mapping from sensor type to required activation status
     *
     * @param requiredSensors list of required sensors
     * @return the mapping
     */
    private EnumMap<SensorType, Boolean> getSensorActivationList(List<SensorType> requiredSensors) {
        // empty sensor activation list
        EnumMap<SensorType, Boolean> sensorActivationList = new EnumMap<>(SensorType.class);

        // default all sensors to off
        for (SensorType sensorType : SensorType.values())
            sensorActivationList.put(sensorType, false);

        // set those to true which are required
        for (SensorType sensorType : requiredSensors)
            sensorActivationList.put(sensorType, true);

        return sensorActivationList;
    }

    /**
     * Apply each sensor state from a sensor activation list
     *
     * @param activationList a mapping from sensor type to its required activation state
     * @return true if all sensor states could be correctly applied
     */
    private boolean applySensorActivationList(EnumMap<SensorType, Boolean> activationList) {
        boolean success = true;
        for (Map.Entry<SensorType, Boolean> sensor : activationList.entrySet())
            success = setSensor(sensor.getKey(), sensor.getValue()) && success;
        return success;
    }

    /**
     * This method tries to dis- or enable a certain sensor.
     *
     * @param sensorType the list of required sensors
     * @return true if the sensor state has been successfully set
     */
    private boolean setSensor(SensorType sensorType, boolean enable) {
        try {
            // are we supposed to enable the sensor?
            if (enable) {
                // already instantiated, just set it running
                if (mSensors.containsKey(sensorType))
                    mSensors.get(sensorType).setRunning(true);
                    // not yet instantiated, instantiate and start
                else
                    instantiateSensor(sensorType);
            }
            // disable the sensor if it was already instantiated
            else if (mSensors.containsKey(sensorType))
                mSensors.get(sensorType).setRunning(false);
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException | IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Re-enable the sensors that were previously temporarily disabled using {@link #temporarilyDisableSensors()}
     */
    public void enableTemporarilyDisabledSensors() {
        applySensorActivationList(mTemporarySensorActivationList);
        mSensorsTempDisabled = false;
    }

    /**
     * Save the sensor activation state, and temporarily disable all of them, until they are activated
     * again with {@link #enableTemporarilyDisabledSensors()}
     */
    public void temporarilyDisableSensors() {
        // assemble a list of required sensors by listing the ones currently enabled
        List<SensorType> requiredSensors = new ArrayList<>();
        for (AbstractSensor sensor : mSensors.values())
            if (sensor.isRegistered())
                requiredSensors.add(sensor.getSensorType());

        // store the old required sensors
        mTemporarySensorActivationList = getSensorActivationList(requiredSensors);
        mSensorsTempDisabled = true;

        // disable all for now by requiring an empty list of sensors
        applySensorActivationList(getSensorActivationList(new ArrayList<SensorType>()));
    }

    /**
     * Instantiate a sensor
     *
     * @param sensorType the sensor type to be initiated
     * @throws NoSuchMethodException     if the sensor could not be instantiated
     * @throws IllegalAccessException    if the sensor could not be instantiated
     * @throws InvocationTargetException if the sensor could not be instantiated
     * @throws InstantiationException    if the sensor could not be instantiated
     * @throws IOException               if the sensor could not be started
     */
    @SuppressWarnings("unchecked")
    private void instantiateSensor(SensorType sensorType) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        Class sensorImplementation = getClassForSensorType(sensorType);

        // find the constructor for this class
        Constructor<?> constructor = sensorImplementation.getConstructor(Context.class);

        // instantiate it
        AbstractSensor instance = (AbstractSensor) constructor.newInstance(mContext);

        // configure
        instance.setRunning(true);
        instance.setDataSink(mDataSink);

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
