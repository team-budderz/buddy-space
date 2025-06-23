package team.budderz.buddyspace.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.domain.notification.service.NotificationService;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;
import team.budderz.buddyspace.infra.database.post.entity.Post;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostEventHandler {

    private final NotificationService notificationService;
    private final MembershipRepository membershipRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostEvent event) {
        if (event == null || event.getPost() == null || event.getWriter() == null) {
            return;
        }
        Post post = event.getPost();
        User user = event.getWriter();

        if (post.getGroup() == null) {
            return;
        }
        Long groupId = post.getGroup().getId();

        List<Membership> members = membershipRepository.findByGroup_IdAndJoinStatus(groupId, JoinStatus.APPROVED);

        for (Membership membership : members) {
            User member = membership.getUser();

            if (member.getId().equals(user.getId())) continue;

            NotificationType type = post.getIsNotice() ?
                    NotificationType.NOTICE :
                    NotificationType.POST;

            NotificationArgs baseArgs = new NotificationArgs(
                    user.getName(),
                    post.getGroup().getName(),
                    null,
                    member.getId(),
                    groupId,
                    post.getId(),
                    null
            );

            notificationService.sendNotice(
                    type,
                    member,
                    post.getGroup(),
                    baseArgs
            );
        }
    }
}
