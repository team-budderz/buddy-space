package team.budderz.buddyspace.api.group.response;

import team.budderz.buddyspace.infra.database.group.entity.Group;

public record GroupInviteResponse(
        Long groupId,
        String groupName,
        String groupDescription,
        String inviteLink
) {
    public static GroupInviteResponse of(Group group, String inviteLink) {
        return new GroupInviteResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                inviteLink
        );
    }
}
