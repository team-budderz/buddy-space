package team.budderz.buddyspace.domain.notification.template;

import org.springframework.stereotype.Component;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.domain.notification.exception.NotificationErrorCode;
import team.budderz.buddyspace.global.exception.BaseException;
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
        if (args == null || args.groupId() == null || args.postId() == null || args.commentId() == null) {
            throw new BaseException(NotificationErrorCode.INVALID_NOTIFICATION_ARGUMENT);
        }
        return String.format("/api/groups/%d/posts/%d/comments/%d", args.groupId(), args.postId(), args.commentId());
    }
}
