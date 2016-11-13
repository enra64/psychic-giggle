package de.ovgu.softwareprojekt.control.commands;

import de.ovgu.softwareprojekt.SensorType;

/**
 * This command may be used to switch sensors on or off
 */
public class SetSensorCommand extends Command {
    /**
     * The sensor that should be switched on or off
     */
    public SensorType sensorType;

    /**
     * Whether to enable or disable the sensor
     */
    public boolean enable;

    /**
     * Create a new command to switch a sensor on or off
     * @param type The sensor that should be switched on or off
     * @param enable Whether to enable or disable the sensor
     */
    public SetSensorCommand(SensorType type, boolean enable) {
        super(CommandType.SetSensor);
        sensorType = type;
        this.enable = enable;
    }
}
