package team.budderz.buddyspace.domain.membership.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                event.requester().getName(),
                event.group().getName(),
                "가입 요청",
                event.leader().getId(),
                event.group().getId(),
                null,
                null
        );

        notificationService.sendNotice(
                NotificationType.GROUP_JOIN_REQUEST,
                event.leader(),
                event.group(),
                args
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMembershipJoinApproved(MembershipJoinApprovedEvent event) {
        NotificationArgs args = new NotificationArgs(
                null,
                event.group().getName(),
                "가입 승인",
                event.requester().getId(),
                event.group().getId(),
                null,
                null
        );

        notificationService.sendNotice(
                NotificationType.GROUP_JOIN_APPROVED,
                event.requester(),
                event.group(),
                args
        );
    }
}
