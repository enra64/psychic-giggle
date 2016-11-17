package de.ovgu.softwareprojekt.control.commands;

/**
 * Created by markus on 17.11.16.
 */

public class ButtonClick extends Command{
    public int mID;

    public ButtonClick(int id){
        super(CommandType.ButtonClick);
        mID = id;
    }
}
