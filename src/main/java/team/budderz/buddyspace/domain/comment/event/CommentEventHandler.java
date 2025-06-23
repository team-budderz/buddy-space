package team.budderz.buddyspace.domain.comment.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.domain.notification.service.NotificationService;
import team.budderz.buddyspace.domain.post.event.PostEvent;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;
import team.budderz.buddyspace.infra.database.post.entity.Post;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentEventHandler {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleComment(CommentEvent event) {
        Comment comment = event.getComment();
        User writer = event.getWriter();
        Post post = comment.getPost();
        User postWriter = post.getUser();

        if (!postWriter.getId().equals(writer.getId())) {
            NotificationArgs args = new NotificationArgs(
                    writer.getName(),
                    post.getGroup().getName(),
                    null,
                    postWriter.getId(),
                    post.getGroup().getId(),
                    post.getId(),
                    comment.getId()
            );

            notificationService.sendNotice(
                    NotificationType.COMMENT,
                    postWriter,
                    post.getGroup(),
                    args
            );
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReply(ReplyEvent event) {
        Comment reply = event.getRecomment();
        User writer = event.getWriter();
        Post post = reply.getPost();
        User postWriter = post.getUser();
        User parentWriter = reply.getParent().getUser();

        // 게시글 작성자에게
        if (!postWriter.getId().equals(writer.getId())) {
            NotificationArgs args = new NotificationArgs(
                    writer.getName(),
                    post.getGroup().getName(),
                    null,
                    postWriter.getId(),
                    post.getGroup().getId(),
                    post.getId(),
                    reply.getId()
            );

            notificationService.sendNotice(
                    NotificationType.REPLY,
                    postWriter,
                    post.getGroup(),
                    args
            );
        }

        // 부모 댓글 작성자에게 (작성자, 게시글 작성자 제외)
        if (!parentWriter.getId().equals(writer.getId())
                && !parentWriter.getId().equals(postWriter.getId())) {
            NotificationArgs args = new NotificationArgs(
                    writer.getName(),
                    post.getGroup().getName(),
                    null,
                    parentWriter.getId(),
                    post.getGroup().getId(),
                    post.getId(),
                    reply.getId()
            );

            notificationService.sendNotice(
                    NotificationType.REPLY,
                    parentWriter,
                    post.getGroup(),
                    args
            );
        }
    }

}
