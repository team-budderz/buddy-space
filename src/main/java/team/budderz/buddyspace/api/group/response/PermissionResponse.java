package team.budderz.buddyspace.api.group.response;

import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

public record PermissionResponse(
        PermissionType type,
        MemberRole role
) {
    public static PermissionResponse from(GroupPermission groupPermission) {
        return new PermissionResponse(
                groupPermission.getType(),
                groupPermission.getRole()
        );
    }
}
