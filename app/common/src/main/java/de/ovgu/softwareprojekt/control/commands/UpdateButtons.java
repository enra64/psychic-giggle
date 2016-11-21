package de.ovgu.softwareprojekt.control.commands;

import java.util.Map;

/**
 * Created by markus on 17.11.16.
 */

public class UpdateButtons extends Command {
    public Map<Integer, String> buttons;

    public UpdateButtons(Map<Integer, String> buttons){
        super(CommandType.AddButton);
        this.buttons = buttons;
    }
}
