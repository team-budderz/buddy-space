package team.budderz.buddyspace.api.group.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

@Schema(description = "모임 기능별 권한 목록 응답 DTO")
public record PermissionResponse(
        @Schema(description = "모임 기능", example = "DELETE_POST")
        PermissionType type,

        @Schema(description = "모임 권한", example = "SUB_LEADER")
        MemberRole role
) {
    public static PermissionResponse from(GroupPermission groupPermission) {
        return new PermissionResponse(
                groupPermission.getType(),
                groupPermission.getRole()
        );
    }
}
