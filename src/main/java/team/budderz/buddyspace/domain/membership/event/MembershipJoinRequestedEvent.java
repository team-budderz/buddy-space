package team.budderz.buddyspace.domain.membership.event;

import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

public record MembershipJoinRequestedEvent(
        Group group,
        User requester,
        User leader
) {
}