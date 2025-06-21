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
        return String.format("%s 님이 새 게시글을 올렸습니다.", args.senderName());
    }

    @Override
    public String generateUrl(NotificationArgs args) {
        return "/api/group/" + args.groupId() + "/posts/" + args.postId();
    }
}
