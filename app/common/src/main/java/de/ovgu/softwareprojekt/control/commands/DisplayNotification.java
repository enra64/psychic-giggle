package de.ovgu.softwareprojekt.control.commands;

/**
 * Created by markus on 10.01.17.
 */

public class DisplayNotification extends AbstractCommand {
    /**
     * used to indentify the device
     */
    public String title;
    public String content;
    public int id;
    public boolean isOnGoing;

    /**
     * command to assign an id to a device
     * @param notificationTitle used to display the title
     * @param notificationContent used to display content
     * @param id used to indetify the notification
     * @param isOnGoing used to decide if notification is removable by user
     */
    public DisplayNotification(int id, String notificationTitle, String notificationContent, boolean isOnGoing) {
        super(CommandType.DisplayNotification);
        this.id = id;
        this.title = notificationTitle;
        this.content = notificationContent;
        this.isOnGoing = isOnGoing;


    }
}
