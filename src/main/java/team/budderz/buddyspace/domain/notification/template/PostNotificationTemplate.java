package team.budderz.buddyspace.domain.notification.template;

import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

@Component
public class PostNotificationTemplate implements NotificationTemplate{
    @Override
    public NotificationType getType() {
        return NotificationType.POST;
    }

    @Override
    public String generateContent(NotificationArgs args) {
        return String.format("새 게시글이 올라왔습니다.", args.senderName());
    }
}
