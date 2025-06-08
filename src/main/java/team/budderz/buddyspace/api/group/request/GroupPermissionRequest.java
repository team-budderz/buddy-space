package team.budderz.buddyspace.api.group.request;

import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

public record GroupPermissionRequest(
        PermissionType type,
        MemberRole role
) {}
