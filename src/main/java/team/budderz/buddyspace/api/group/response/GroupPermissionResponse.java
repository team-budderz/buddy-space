package team.budderz.buddyspace.api.group.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;

import java.util.List;

@Schema(description = "모임 기능별 권한 응답 DTO")
public record GroupPermissionResponse(
        @Schema(description = "모임 식별자", example = "1")
        Long groupId,

        @Schema(description = "모임 이름", example = "벗터즈")
        String groupName,

        @Schema(description = "모임 기능별 권한 목록")
        List<PermissionResponse> permissions

) {
    public static GroupPermissionResponse of(Group group, List<GroupPermission> permission) {
        return new GroupPermissionResponse(
                group.getId(),
                group.getName(),
                permission.stream()
                        .map(PermissionResponse::from)
                        .toList()
        );
    }
}
