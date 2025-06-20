package team.budderz.buddyspace.domain.notification.template;

import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

@Component
public class NoticeNotificationTemplate implements NotificationTemplate{
    @Override
    public NotificationType getType() {
        return NotificationType.NOTICE;
    }

    @Override
    public String generateContent(NotificationArgs args) {
        return String.format("[%s] 그룹에 새 공지가 등록되었습니다.", args.groupName());
    }

    @Override
    public String generateUrl(NotificationArgs args) {
        return "/api/group/" + args.groupId() + "/posts/" + args.postId();
    }
}
