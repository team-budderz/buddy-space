package team.budderz.buddyspace.api.group.response;

import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;

/**
 * 모임 생성 응답 DTO
 */
public record SaveGroupResponse(

        String groupName,
        String groupCoverImageUrl,
        GroupAccess groupAccess,
        GroupType groupType,
        GroupInterest groupInterest
) {

    public static SaveGroupResponse from(Group group) {
        return new SaveGroupResponse(
                group.getName(),
                group.getCoverImageUrl(),
                group.getAccess(),
                group.getType(),
                group.getInterest()
        );
    }
}
