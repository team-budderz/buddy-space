package team.budderz.buddyspace.api.membership.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.membership.entity.JoinPath;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

import java.time.LocalDateTime;

@Schema(description = "모임 멤버 응답 DTO")
public record MemberResponse(
        @Schema(description = "멤버 식별자", example = "1")
        Long id,

        @Schema(description = "멤버 이름", example = "김하나")
        String name,

        @Schema(description = "멤버 프로필 이미지 url", example = "https://profile.image")
        String profileImageUrl,

        @Schema(description = "모임 권한", example = "SUB_LEADER")
        MemberRole role,

        @Schema(description = "모임 가입 상태", example = "APPROVED")
        JoinStatus status,

        @Schema(description = "모임 가입 경로", example = "REQUEST")
        JoinPath joinPath,

        @Schema(description = "모임 가입 일시", example = "2025-06-16T10:04:23.391439")
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
