package team.budderz.buddyspace.api.membership.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

/**
 * 멤버 권한 변경 요청 DTO
 */
@Schema(description = "멤버 권한 설정 요청 DTO")
public record MemberRoleRequest(

        @NotNull(message = "멤버 권한 입력은 필수입니다.")
        @Schema(description = "모임 권한", example = "SUB_LEADER")
        MemberRole role
) {}
