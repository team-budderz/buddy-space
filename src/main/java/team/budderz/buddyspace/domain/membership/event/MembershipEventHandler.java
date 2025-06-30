package team.budderz.buddyspace.domain.membership.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team.budderz.buddyspace.api.notification.response.NotificationArgs;
import team.budderz.buddyspace.domain.notification.service.NotificationService;
import team.budderz.buddyspace.infra.database.notification.entity.NotificationType;

@Component
@RequiredArgsConstructor
public class MembershipEventHandler {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMembershipJoinRequest(MembershipJoinRequestedEvent event) {
        NotificationArgs args = new NotificationArgs(
                null,
                null,
                "가입 요청",
                event.leaderId().getId(),
                event.groupId(),
                null,
                null
        );

        notificationService.sendNotice(
                NotificationType.GROUP_JOIN_REQUEST,
                event.leaderId(),
                null,
                args
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMembershipJoinApproved(MembershipJoinApprovedEvent event) {
        NotificationArgs args = new NotificationArgs(
                null,
                null,
                "가입 승인",
                event.requesterId().getId(),
                event.groupId(),
                null,
                null
        );

        notificationService.sendNotice(
                NotificationType.GROUP_JOIN_APPROVED,
                event.requesterId(),
                null,
                args
        );
    }
}
