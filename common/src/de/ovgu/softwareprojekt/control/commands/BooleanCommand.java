package de.ovgu.softwareprojekt.control.commands;

/**
 * Created by arne on 11/13/16.
 */
public class BooleanCommand extends Command {
    public boolean value;

    public BooleanCommand(CommandType type, boolean value) {
        super(type);
        this.value = value;
    }
}
