package team.budderz.buddyspace.api.group.response;

import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;

import java.util.List;

public record GroupPermissionResponse(
        Long groupId,
        String groupName,
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
