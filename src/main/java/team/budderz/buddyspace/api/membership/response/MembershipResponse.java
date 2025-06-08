package team.budderz.buddyspace.api.membership.response;

import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;

import java.util.List;

public record MembershipResponse(
        Long groupId,
        String groupName,
        List<MemberResponse> members
) {
    public static MembershipResponse of(Group group, List<Membership> members) {
        return new MembershipResponse(
                group.getId(),
                group.getName(),
                members.stream()
                        .map(MemberResponse::from)
                        .toList()
        );
    }
}
