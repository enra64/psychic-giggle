package de.ovgu.softwareprojekt.control.commands;

import java.util.Map;

/**
 * Created by markus on 17.11.16.
 */

public class UpdateButtonsMap extends AbstractCommand {
    public Map<Integer, String> buttons;

    public UpdateButtonsMap(Map<Integer, String> buttons){
        super(CommandType.UpdateButtonsMap);
        this.buttons = buttons;
    }
}
