package team.budderz.buddyspace.api.membership.response;

import team.budderz.buddyspace.infra.database.membership.entity.JoinPath;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

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
    public static MemberResponse of(
            Long id,
            String name,
            String profileImageUrl,
            MemberRole role,
            JoinStatus status,
            JoinPath joinPath,
            LocalDateTime joinedAt
    ) {
        return new MemberResponse(id, name, profileImageUrl, role, status, joinPath, joinedAt);
    }
}
