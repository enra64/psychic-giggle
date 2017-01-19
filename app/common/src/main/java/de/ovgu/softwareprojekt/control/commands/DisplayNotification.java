package de.ovgu.softwareprojekt.control.commands;

/**
 * This command may be used by the server to request the display of a notification.
 */
public class DisplayNotification extends AbstractCommand {
    /**
     * Title of the notification
     */
    public String title;

    /**
     * Content of the notification
     */
    public String content;

    /**
     * notification id
     */
    public int id;

    /**
     * True if the notification should be ongoing
     */
    public boolean isOnGoing;

    /**
     * command to assign an id to a device
     * @param notificationTitle title of the notification
     * @param notificationContent content of the notification
     * @param id used to identify the notification
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
