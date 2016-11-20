package de.ovgu.softwareprojektapp.sensors;

import android.content.Context;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

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
     * @param context required for interaction with android sensor system
     */
    public SensorHandler(Context context){
        mContext = context;
    }

    /**
     * This method tries to dis- or enable a certain sensor.
     *
     * @param dataSink the datasink the sensor should report to
     * @param sensorType the sensor type we wish to use
     * @param enable true if the sensor should send data
     * @return true if the sensor state has been successfully set
     */
    @SuppressWarnings("unchecked")
    public boolean setRunning(DataSink dataSink, SensorType sensorType, boolean enable) {
        try {
            Class sensorImplementation = getClassForSensorType(sensorType);

            // if we already have an instance of this class, there is no need to get another on
            if (mSensors.containsKey(sensorType)) {
                mSensors.get(sensorType).setRunning(enable);
            }
            // we need to create a new instance of this sensor type
            else {
                // find the constructor for this class
                Constructor<?> constructor = sensorImplementation.getConstructor(Context.class);

                // instantiate it
                AbstractSensor instance = (AbstractSensor) constructor.newInstance(mContext);

                // configure
                instance.setRunning(enable);
                instance.setDataSink(dataSink);

                // put into sensor list to avoid next instantiation
                mSensors.put(sensorType, instance);
            }
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException | IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Close all sensors (unregister them)
     */
    public void closeAll(){
        for(AbstractSensor abstractSensor : mSensors.values())
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
