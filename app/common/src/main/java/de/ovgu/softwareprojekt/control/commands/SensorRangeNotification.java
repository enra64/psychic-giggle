package de.ovgu.softwareprojekt.control.commands;


import de.ovgu.softwareprojekt.SensorType;

@SuppressWarnings("WeakerAccess")
public class SensorRangeNotification extends AbstractCommand {
    public SensorType type;
    public float range;

    public SensorRangeNotification(SensorType type, float sensorRange){
        super(CommandType.SensorRangeNotification);
        this.type = type;
        range = sensorRange;
    }
}
