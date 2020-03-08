import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class GeneratorErrorsNotifier{

    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Generator errors", NotificationDisplayType.BALLOON, true);

    public Notification notify(String message){
        return notify(null, message);
    }

    public Notification notify(Project project, String message){
        final Notification notification = NOTIFICATION_GROUP.createNotification(message, NotificationType.ERROR);
        notification.notify(project);
        return notification;
    }

}
