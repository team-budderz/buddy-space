package team.budderz.buddyspace.domain.notification.template;

import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.domain.notification.exception.NotificationErrorCode;
import team.budderz.buddyspace.global.exception.BaseException;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

@Component
public class JoinRequestNotificationTemplate implements NotificationTemplate{
    @Override
    public NotificationType getType() {
        return NotificationType.GROUP_JOIN_REQUEST;
    }

    @Override
    public String generateContent(NotificationArgs args) {
        return String.format("%s 님이 가입 요청을 보냈습니다.", args.senderName());
    }

    @Override
    public String generateUrl(NotificationArgs args) {
        if (args == null || args.groupId() == null || args.postId() == null || args.memberId() == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_ARGUMENT);
        }
        return String.format("/api/groups/%d/members/%d/approve", args.groupId(), args.memberId());
    }
}
