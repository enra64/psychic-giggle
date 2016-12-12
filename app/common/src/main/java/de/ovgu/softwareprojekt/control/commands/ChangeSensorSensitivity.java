package de.ovgu.softwareprojekt.control.commands;


import de.ovgu.softwareprojekt.SensorType;

@SuppressWarnings("WeakerAccess")
public class ChangeSensorSensitivity extends AbstractCommand {
    public SensorType sensorType;
    public int sensitivity;

    public ChangeSensorSensitivity(SensorType tag, int progress){
        super(CommandType.ChangeSensorSensitivity);
        sensorType = tag;
        sensitivity = progress;
    }
}
