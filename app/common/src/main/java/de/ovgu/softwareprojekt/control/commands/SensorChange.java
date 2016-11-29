package de.ovgu.softwareprojekt.control.commands;


import de.ovgu.softwareprojekt.SensorType;

public class SensorChange extends Command {
    public SensorType mTag;
    public int mProgress;

    public SensorChange(SensorType tag, int progress){
        super(CommandType.SensorChange);
        mTag = tag;
        mProgress = progress;
    }
}
