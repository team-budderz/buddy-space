package team.budderz.buddyspace.api.group.request;

import jakarta.validation.constraints.NotNull;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

public record GroupPermissionRequest(
        @NotNull(message = "권한을 설정할 기능 유형 입력은 필수입니다.")
        PermissionType type,

        @NotNull(message = "모임 회원의 권한 입력은 필수입니다.")
        MemberRole role
) {}
