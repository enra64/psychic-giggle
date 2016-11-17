package de.ovgu.softwareprojekt.control.commands;

/**
 * Created by markus on 17.11.16.
 */

public class AddButton extends Command {
    public String mName;
    public int mID;

    public AddButton(String name, int id){
        super(CommandType.AddButton);
        this.mName = name;
        this.mID = id;
    }
}
