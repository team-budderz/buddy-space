package team.budderz.buddyspace.api.notification.response;

import team.budderz.buddyspace.infra.database.notification.entity.Notification;

import java.time.LocalDateTime;

public record FindsNotificationResponse(
        String content,
        String groupName,
        LocalDateTime createdAt
) {
    public static FindsNotificationResponse from(Notification notification) {
        return new FindsNotificationResponse(
                notification.getContent(),
                notification.getGroup() != null ? notification.getGroup().getName() : null,
                notification.getCreatedAt()
        );
    }
}