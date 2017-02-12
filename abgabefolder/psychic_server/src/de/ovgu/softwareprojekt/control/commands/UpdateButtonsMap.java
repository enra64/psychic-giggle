package de.ovgu.softwareprojekt.control.commands;

import java.util.Map;

/**
 * This command is used for setting the buttons displayed in the app via a mapping of psychic button ids
 * and button texts.
 */
public class UpdateButtonsMap extends AbstractCommand {
    /**
     * The (psychic button id {@literal ->} button text) mapping used for creating the buttons
     */
    public Map<Integer, String> buttons;

    /**
     * Create a new {@link UpdateButtonsMap} command
     *
     * @param buttons the (psychic button id {@literal ->} button text) mapping used for creating the buttons
     */
    public UpdateButtonsMap(Map<Integer, String> buttons) {
        super(CommandType.UpdateButtonsMap);
        this.buttons = buttons;
    }
}
