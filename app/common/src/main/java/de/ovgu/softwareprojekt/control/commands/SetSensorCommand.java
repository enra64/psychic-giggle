package de.ovgu.softwareprojekt.control.commands;

import java.util.Set;

import de.ovgu.softwareprojekt.SensorType;

/**
 * This command may be used to switch sensors on or off
 */
public class SetSensorCommand extends Command {
    /**
     * The set of sensors required to be enabled
     */
    public Set<SensorType> requiredSensors;

    /**
     * Create a new command to set the set of required sensors
     * @param requiredSensors The list of sensors the application currently requires
     */
    public SetSensorCommand(Set<SensorType> requiredSensors) {
        super(CommandType.SetSensor);
        this.requiredSensors = requiredSensors;
    }
}
