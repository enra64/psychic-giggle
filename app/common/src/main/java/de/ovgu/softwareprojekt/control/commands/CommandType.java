package de.ovgu.softwareprojekt.control.commands;

/**
 * This enum is used to identify commands. This may provide information about possible casting of a command.
 */
public enum CommandType {
    ConnectionRequest,
    ConnectionRequestResponse,
    SetSensor,
    EndConnection,
    ButtonClick,
    ChangeSensorSensitivity,
    ResetToCenter,
    ConnectionAliveCheck,
    SensorRangeNotification,
    UpdateButtonsMap,
    UpdateButtonsXML,
    SetSensorSpeed,
    DisplayNotification

}
