package team.budderz.buddyspace.domain.notification.template;

import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.domain.notification.exception.NotificationErrorCode;
import team.budderz.buddyspace.global.exception.BaseException;
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
        if (args == null || args.groupId() == null || args.postId() == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_ARGUMENT);
        }
        return String.format("/api/groups/%d/posts/%d", args.groupId(), args.postId());
    }
}
