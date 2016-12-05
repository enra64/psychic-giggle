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
    private boolean mSensorsTempDisabled = false;

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
     * Update the sensor speed for a given sensor
     * @param sensorType the sensor type that should change its speed
     * @param sensorSpeed the speed the sensor should switch to
     * @return true if the setting succeeded
     */
    public boolean setSensorSpeed(SensorType sensorType, SetSensorSpeed.SensorSpeed sensorSpeed) {
        try {
            // get the class
            Class<?> sensorClass = getClass(sensorType);

            // call setSensorSpeed with sensor speed argument
            sensorClass.getDeclaredMethod("setSensorSpeed", SetSensorSpeed.SensorSpeed.class).invoke(null, sensorSpeed);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }
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
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | InvocationTargetException | IllegalAccessException | IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Wrapper function for {@link #enableTemporarilyDisabledSensors()} and
     * {@link #temporarilyDisableSensors()}.
     *
     * @param enable true if the sensors should be enabled
     */
    public void temporarilySetSensors(boolean enable) {
        if (enable)
            enableTemporarilyDisabledSensors();
        else
            temporarilyDisableSensors();
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
     * @throws ClassNotFoundException    if no sensor class with a name matching the enum value could be found
     */
    @SuppressWarnings("unchecked")
    private void instantiateSensor(SensorType sensorType)
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException,
            IOException,
            ClassNotFoundException {
        // find the constructor for this sensor type
        Constructor<?> constructor = getClass(sensorType).getConstructor(Context.class);

        // instantiate it
        AbstractSensor instance = (AbstractSensor) constructor.newInstance(mContext);

        // configure
        instance.setRunning(true);
        instance.setDataSink(mDataSink);

        // put into sensor list to avoid next instantiation
        mSensors.put(sensorType, instance);
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
