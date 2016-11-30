package de.ovgu.softwareprojekt.control.commands;

import java.io.Serializable;

/**
 * The base class for commands. All control messages subclass this so we can send them via a {@link de.ovgu.softwareprojekt.control.CommandConnection}.
 */
public abstract class AbstractCommand implements Serializable {
    /**
     * The command type. May not be changed after instantiating a command
     */
    private CommandType mCommandType;

    /**
     * Create a new command with the given command type
     */
    protected AbstractCommand(CommandType type){
        mCommandType = type;
    }

    /**
     * Retrieve the {@link CommandType} this command is. The command may be cast according to this, for example to a
     * {@link SetSensorCommand} if the CommandType is SetSensor
     */
    public CommandType getCommandType(){
        return mCommandType;
    }
}
