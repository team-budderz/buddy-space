package team.budderz.buddyspace.api.group.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;

/**
 * 모임 생성/수정 응답 DTO
 */
public record GroupResponse(
        Long groupId,
        String groupName,
        String groupDescription,
        String groupCoverImageUrl,
        GroupAccess groupAccess,
        GroupType groupType,
        GroupInterest groupInterest,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String groupAddress,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Boolean isNeighborhoodAuthRequired
) {
    public static GroupResponse from(Group group, String coverImageUrl) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                coverImageUrl,
                group.getAccess(),
                group.getType(),
                group.getInterest(),
                group.getAddress(),
                group.isNeighborhoodAuthRequired()
        );
    }
}
