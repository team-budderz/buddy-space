package team.budderz.buddyspace.api.group.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

@Schema(description = "모임 권한 설정 요청 DTO")
public record GroupPermissionRequest(
        @NotNull(message = "권한을 설정할 기능 유형 입력은 필수입니다.")
        @Schema(description = "모임 기능", example = "DELETE_POST")
        PermissionType type,

        @NotNull(message = "모임 회원의 권한 입력은 필수입니다.")
        @Schema(description = "모임 권한", example = "SUB_LEADER")
        MemberRole role
) {}
