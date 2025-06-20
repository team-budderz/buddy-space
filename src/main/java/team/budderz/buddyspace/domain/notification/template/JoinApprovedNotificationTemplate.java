package team.budderz.buddyspace.domain.notification.template;

import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

@Component
public class JoinApprovedNotificationTemplate implements NotificationTemplate{
    @Override
    public NotificationType getType() {
        return NotificationType.GROUP_JOIN_APPROVED;
    }

    @Override
    public String generateContent(NotificationArgs args) {
        return "관리자가 가입 요청을 수락했습니다.";
    }
}
