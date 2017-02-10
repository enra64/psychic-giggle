package de.ovgu.softwareprojekt.control.commands;

import java.util.EnumMap;

import de.ovgu.softwareprojekt.SensorType;

/**
 * Created by Ulrich
 *
 * This commands is responsible for displaying information about the sensor usage
 */

public class SensorDescription extends AbstractCommand {

    /**
     * The {@link SensorType} that is regarded
     */
    public SensorType usedSensor;
    /**
     * The information about the used SensorType
     */
    public  String sensorDescription;

    /**
     * Create a new {@link SensorDescription} command
     *
     * @param type the {@link SensorType} that is regarded
     * @param description the information about sensor
     */
    public SensorDescription(SensorType type, String description) {
        super(CommandType.SensorDescription);
        usedSensor = type;
        sensorDescription = description;
    }
}
