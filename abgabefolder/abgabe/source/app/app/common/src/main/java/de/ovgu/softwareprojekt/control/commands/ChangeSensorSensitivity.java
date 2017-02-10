package de.ovgu.softwareprojekt.control.commands;


import de.ovgu.softwareprojekt.SensorType;

/**
 * Command used for notifying the server of changed sensitivity requirements
 */
@SuppressWarnings("WeakerAccess")
public class ChangeSensorSensitivity extends AbstractCommand {
    /**
     * The sensor whose sensitivity should be changed
     */
    public SensorType sensorType;

    /**
     * The requested sensitivity. Between 0 and 100.
     */
    public int sensitivity;

    /**
     * Create a new {@link ChangeSensorSensitivity} comand
     * @param tag the {@link SensorType} whose sensitvity should be changed
     * @param progress the requested sensitivity. Between 0 and 100.
     */
    public ChangeSensorSensitivity(SensorType tag, int progress){
        super(CommandType.ChangeSensorSensitivity);
        sensorType = tag;
        sensitivity = progress;
    }
}
