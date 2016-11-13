package de.ovgu.softwareprojekt.control.commands;

import java.io.Serializable;

public class Command implements Serializable {
    private CommandType mCommandType;

    protected Command(CommandType type){
        mCommandType = type;
    }

    public CommandType getCommandType(){
        return mCommandType;
    }
}
