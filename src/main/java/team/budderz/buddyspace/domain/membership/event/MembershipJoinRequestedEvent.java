package team.budderz.buddyspace.domain.membership.event;

import team.budderz.buddyspace.infra.database.user.entity.User;

public record MembershipJoinRequestedEvent(
        Long groupId,
        User requester,
        User leaderId
) {
}