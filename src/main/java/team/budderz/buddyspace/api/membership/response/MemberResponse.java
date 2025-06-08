package team.budderz.buddyspace.api.membership.response;

import team.budderz.buddyspace.infra.database.membership.entity.JoinPath;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String name,
        String profileImageUrl,
        MemberRole role,
        JoinStatus status,
        JoinPath joinPath,
        LocalDateTime joinedAt
) {
    public static MemberResponse from(Membership membership) {
        return new MemberResponse(
                membership.getUser().getId(),
                membership.getUser().getName(),
                membership.getUser().getImageUrl(),
                membership.getMemberRole(),
                membership.getJoinStatus(),
                membership.getJoinPath(),
                membership.getJoinedAt()
        );
    }
}
