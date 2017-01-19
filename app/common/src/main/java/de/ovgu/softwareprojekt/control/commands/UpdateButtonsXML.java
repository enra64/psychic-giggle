package de.ovgu.softwareprojekt.control.commands;

/**
 * This command is used to set the buttons displayed in the app using a subset of allowed android
 * layout resource files
 */
public class UpdateButtonsXML extends AbstractCommand {
    /**
     * This string should contain the android layout xml file contents
     */
    public String xmlContent;

    /**
     * Create a new {@link UpdateButtonsXML} command.
     *
     * @param xml a string representation of the android layout xml file content
     */
    public UpdateButtonsXML(String xml) {
        super(CommandType.UpdateButtonsXML);
        this.xmlContent = xml;
    }
}
