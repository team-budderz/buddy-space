package team.budderz.buddyspace.api.notification.response;

import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        String content,
        String previewContent,
        String groupName,
        String senderName,
        String senderImageUrl,
        boolean isRead,
        String url,
        LocalDateTime createdAt,
        NotificationType type
) {}
