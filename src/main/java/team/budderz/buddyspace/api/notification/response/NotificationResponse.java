package team.budderz.buddyspace.api.notification.response;

import team.budderz.buddyspace.infra.database.notification.entity.Notification;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        String content,
        String url,
        boolean isRead,
        String groupName,
        LocalDateTime createdAt,
        NotificationType type
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getContent(),
                notification.getUrl(),
                notification.isRead(),
                notification.getGroup() != null ? notification.getGroup().getName() : null,
                notification.getCreatedAt(),
                notification.getType()
        );
    }
}
