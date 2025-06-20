package team.budderz.buddyspace.domain.notification.template;

import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

public interface NotificationTemplate {
    NotificationType getType();
    String generateContent(NotificationArgs args);
    String generateUrl(NotificationArgs args);
}