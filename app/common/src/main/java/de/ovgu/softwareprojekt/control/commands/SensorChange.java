package de.ovgu.softwareprojekt.control.commands;


import de.ovgu.softwareprojekt.SensorType;

public class SensorChange extends Command {
    SensorType mTag;
    int mProgress;

    public SensorChange(SensorType tag, int progress){
        super(CommandType.SensorChange);
        mTag = tag;
        mProgress = progress;
    }
}
