package team.budderz.buddyspace.api.group.response;

import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;

/**
 * 모임 목록 조회 응답 DTO
 */
public record GroupListResponse(
        Long groupId,
        String groupName,
        String groupDescription,
        String groupCoverImageUrl,
        GroupType groupType,
        GroupInterest groupInterest,
        Long memberCount
) {}
