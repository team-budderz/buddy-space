package team.budderz.buddyspace.domain.notification.template;

import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

@Component
public class CommentNotificationTemplate implements NotificationTemplate{
    @Override
    public NotificationType getType() {
        return NotificationType.COMMENT;
    }

    @Override
    public String generateContent(NotificationArgs args) {
        return String.format("%s 님이 내 게시글에 댓글을 남겼습니다.", args.senderName());
    }

    @Override
    public String generateUrl(NotificationArgs args) {
        return "/api/group/" + args.groupId() + "/posts/" + args.postId() + "/comments/" + args.commentId();
    }
}
