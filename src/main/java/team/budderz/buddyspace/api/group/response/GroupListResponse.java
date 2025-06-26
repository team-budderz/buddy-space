package team.budderz.buddyspace.api.group.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;

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
        Long memberCount,
        JoinStatus joinStatus,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long coverAttachmentId
) {
    public GroupListResponse withCoverImageUrl(String url) {
        return new GroupListResponse(
                this.groupId,
                this.groupName,
                this.groupDescription,
                url,
                this.groupType,
                this.groupInterest,
                this.memberCount,
                this.joinStatus,
                null
        );
    }
}
