/**
 * @author Carlos Antonio McNulty
 */

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;


public class GeneratorErrorsNotifier{

    // Create notification group for error
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Generator errors", NotificationDisplayType.BALLOON, true);


    /**
     *
     * @param message text for notification
     * @return Notification object
     */
    public Notification notify(String message){
        return notify(null, message);
    }


    /**
     * Creates a notification object
     * @param project active project to show notification
     * @param message text for notification
     * @return Notification object
     */
    public Notification notify(Project project, String message){

        // Create notification from associated group
        final Notification notification = NOTIFICATION_GROUP.createNotification(message, NotificationType.ERROR);
        notification.notify(project);
        return notification;
    }

}
