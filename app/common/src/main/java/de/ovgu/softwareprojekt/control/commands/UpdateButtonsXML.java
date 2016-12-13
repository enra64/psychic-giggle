package de.ovgu.softwareprojekt.control.commands;

/**
 * Created by markus on 12.12.16.
 */

public class UpdateButtonsXML extends AbstractCommand{
    public String xmlContent;

    public UpdateButtonsXML(String xml) {
        super(CommandType.UpdateButtonsXML);
        this.xmlContent = xml;
    }
}
