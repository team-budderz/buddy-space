package team.budderz.buddyspace.domain.notification.template;

import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

@Component
public class ReplyNotificationTemplate implements NotificationTemplate{
    @Override
    public NotificationType getType() {
        return NotificationType.REPLY;
    }

    @Override
    public String generateContent(NotificationArgs args) {
        return String.format("%s 님이 내 댓글에 댓글을 남겼습니다.", args.senderName());
    }
}
