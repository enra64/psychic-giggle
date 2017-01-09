package de.ovgu.softwareprojektapp.sensors;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.SetSensorSpeed;
import de.ovgu.softwareprojektapp.BuildConfig;

/**
 * Handle instantiation and storage of sensor classes, as well as temporarily disabling them. Uses
 * reflection to find classes for the different SensorType values.
 */
public class SensorHandler {
    /**
     * This map stores already instantiated sensor classes
     */
    private EnumMap<SensorType, AbstractSensor> mSensors = new EnumMap<>(SensorType.class);

    /**
     * The list of sensor activations. May be updated during the de-activated phase.
     */
    private EnumMap<SensorType, Boolean> mTemporarySensorActivationList = new EnumMap<>(SensorType.class);

    /**
     * True if the sensors are all (possibly temporarily) unregistered
     */
    private boolean mSensorsSuspended = false;

    /**
     * Context stored for android interaction purposes
     */
    private Context mContext;

    /**
     * The data sink given to every sensor.
     */
    private DataSink mDataSink;

    /**
     * Create a new SensorHandler
     *
     * @param context  required for interaction with android sensor system
     * @param dataSink required for pushing the data somewhere
     */
    public SensorHandler(@NonNull Context context, @NonNull DataSink dataSink) {
        mContext = context;
        mDataSink = dataSink;
    }

    @SuppressWarnings("unchecked")
    public void setRunning(List<SensorType> requiredSensors) throws SensorNotFoundException {
        // if the sensors are currently disabled, only update the list of sensors that will be
        // enabled after starting them again
        if (mSensorsSuspended)
            mTemporarySensorActivationList = getActivatedSensors(requiredSensors);
        else
            // try to apply the list of required sensors
            applySensorActivationList(getActivatedSensors(requiredSensors));
    }

    /**
     * Update the sensor speed for a given sensor
     *
     * @param sensorType  the sensor type that should change its speed
     * @param sensorSpeed the speed the sensor should switch to
     * @return true if the setting succeeded
     */
    public boolean setSensorSpeed(SensorType sensorType, SetSensorSpeed.SensorSpeed sensorSpeed) {
        try {
            // because java does not allow abstract static methods, we have to instantiate the
            // sensor if we want to set its static member variable defining the speed -.-
            AbstractSensor sensor = getSensor(sensorType);

            // change speed
            sensor.setSensorSpeed(sensorSpeed);
            return true;

            // if anything throws, we failed.
        } catch (InstantiationException | IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    /**
     * Create a mapping from sensor type to required activation status
     *
     * @param requiredSensors list of required sensors
     * @return the mapping
     */
    private EnumMap<SensorType, Boolean> getActivatedSensors(List<SensorType> requiredSensors) {
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
     * @throws SensorNotFoundException if a sensor could not be instantiated
     */
    private void applySensorActivationList(EnumMap<SensorType, Boolean> activationList) throws SensorNotFoundException {
        for (Map.Entry<SensorType, Boolean> sensor : activationList.entrySet())
            setSensor(sensor.getKey(), sensor.getValue());
    }

    /**
     * returns a EnumMap with all sensors
     */
    public EnumMap<SensorType, AbstractSensor> getSensors()
    {
        return mSensors;
    }

    /**
     * This method tries to dis- or enable a certain sensor.
     *
     * @param sensorType the list of required sensors
     * @throws SensorNotFoundException if the sensor could not be instantiated
     */
    private void setSensor(SensorType sensorType, boolean enable) throws SensorNotFoundException {
        try {
            // are we supposed to enable the sensor?
            if (enable) {
                // instantiate the sensor
                AbstractSensor sensor = getSensor(sensorType);

                // set it running
                sensor.setDataSink(mDataSink);
                sensor.setRunning(true);
            }
            // disable the sensor if it was already instantiated
            else if (mSensors.containsKey(sensorType))
                mSensors.get(sensorType).setRunning(false);

        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | InvocationTargetException | IllegalAccessException | IOException e) {
            throw new SensorNotFoundException("could not " + (enable ? "enable " : "disable ") + "sensor", sensorType);
        }
    }

    /**
     * Wrapper function for {@link #wakeUpSensors()} and
     * {@link #suspendSensors()}.
     *
     * @param enable true if the sensors should be enabled
     */
    public void temporarilySetSensors(boolean enable) {
        if (enable)
            wakeUpSensors();
        else
            suspendSensors();
    }

    /**
     * Re-enable the sensors that were previously temporarily disabled using {@link #suspendSensors()}
     */
    public void wakeUpSensors() {
        try {
            applySensorActivationList(mTemporarySensorActivationList);
        } catch (SensorNotFoundException e) {
            // we catch this without further reporting, because it should be impossible,
            // since the sensors were already instantiated
            e.printStackTrace();
        }
        mSensorsSuspended = false;
    }

    /**
     * Save the sensor activation state, and temporarily disable all of them, until they are activated
     * again with {@link #wakeUpSensors()}
     */
    public void suspendSensors() {
        // assemble a list of required sensors by listing the ones currently enabled
        List<SensorType> requiredSensors = new ArrayList<>();
        for (AbstractSensor sensor : mSensors.values())
            if (sensor.isRegistered())
                requiredSensors.add(sensor.getSensorType());

        // store the old required sensors
        mTemporarySensorActivationList = getActivatedSensors(requiredSensors);
        mSensorsSuspended = true;

        // disable all for now by requiring an empty list of sensors
        try {
            applySensorActivationList(getActivatedSensors(new ArrayList<SensorType>()));
        } catch (SensorNotFoundException ignored) {
            // we catch this without further reporting, because it should be impossible,
            // since the sensors were already instantiated
            ignored.printStackTrace();
        }
    }

    /**
     * Retrieve a sensor. If it is not yet instantiated, return a new instance.
     *
     * @param sensorType the sensor type to be initiated
     * @throws NoSuchMethodException     if the sensor could not be instantiated
     * @throws IllegalAccessException    if the sensor could not be instantiated
     * @throws InvocationTargetException if the sensor could not be instantiated
     * @throws InstantiationException    if the sensor could not be instantiated
     * @throws IOException               if the sensor could not be started
     * @throws ClassNotFoundException    if no sensor class with a name matching the enum value could be found
     */
    @SuppressWarnings("unchecked")
    private AbstractSensor getSensor(SensorType sensorType)
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException,
            IOException,
            ClassNotFoundException {

        // only do an instantiation if we dont yet have a
        if (!mSensors.containsKey(sensorType)) {
            // find the constructor for this sensor type
            Constructor<?> constructor = getClass(sensorType).getConstructor(Context.class);

            // put into sensor list to avoid next instantiation
            mSensors.put(sensorType, (AbstractSensor) constructor.newInstance(mContext));
        }

        // retrieve the sensor instance
        return mSensors.get(sensorType);
    }

    public float getRange(SensorType sensorType) throws SensorNotFoundException {
        try {
            return getSensor(sensorType).getRange();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException | ClassNotFoundException | InstantiationException e) {
            throw new SensorNotFoundException("Could not get range", sensorType);
        }
    }

    /**
     * Get a Class for a sensor type
     *
     * @param sensorType the sensor type we need
     * @return a Class instance corresponding to the given sensor type
     * @throws ClassNotFoundException if the sensor could not be found. This indicates a sensor type
     *                                that is not in the correct package, or does not have the same
     *                                name as in the enum
     */
    private Class getClass(SensorType sensorType) throws ClassNotFoundException {
        String implementationsPackage = getClass().getPackage().getName() + ".implementations.";
        return Class.forName(implementationsPackage + sensorType.name());
    }

    /**
     * Close all sensors (unregister them)
     */
    public void closeAll() {
        for (AbstractSensor abstractSensor : mSensors.values())
            abstractSensor.close();
    }
}
