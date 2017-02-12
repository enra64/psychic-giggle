package de.ovgu.softwareprojekt.control.commands;

import java.util.List;

import de.ovgu.softwareprojekt.SensorType;

/**
 * This command may be used to switch sensors on or off
 */
public class SetSensorCommand extends AbstractCommand {
    /**
     * The set of sensors required to be enabled
     */
    public List<SensorType> requiredSensors;

    /**
     * Create a new command to set the set of required sensors
     * @param requiredSensors The list of sensors the application currently requires
     */
    public SetSensorCommand(List<SensorType> requiredSensors) {
        super(CommandType.SetSensor);
        this.requiredSensors = requiredSensors;
    }
}
