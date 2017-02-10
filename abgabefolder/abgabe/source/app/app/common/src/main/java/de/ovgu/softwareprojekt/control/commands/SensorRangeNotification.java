package de.ovgu.softwareprojekt.control.commands;


import de.ovgu.softwareprojekt.SensorType;

/**
 * This command is sent by the app to inform the server about the range of a sensor, which is
 * necessary to normalize data
 */
@SuppressWarnings("WeakerAccess")
public class SensorRangeNotification extends AbstractCommand {
    /**
     * The {@link SensorType} that is regarded
     */
    public SensorType type;

    /**
     * The range as reported by android
     */
    public float range;

    /**
     * Create a new {@link SensorRangeNotification} command
     *
     * @param type the {@link SensorType} that is regarded
     * @param sensorRange the range of the sensor as reported by android
     */
    public SensorRangeNotification(SensorType type, float sensorRange) {
        super(CommandType.SensorRangeNotification);
        this.type = type;
        range = sensorRange;
    }
}
