package team.budderz.buddyspace.domain.membership.event;

import team.budderz.buddyspace.infra.database.user.entity.User;

public record MembershipJoinApprovedEvent(
        Long groupId,
        User requesterId,
        User leaderId
    ) {
}