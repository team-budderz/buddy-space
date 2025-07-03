package team.budderz.buddyspace.api.group.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;

/**
 * 모임 목록 조회 응답 DTO
 */
@Schema(description = "모임 목록 조회 응답 DTO")
public record GroupListResponse(
        @Schema(description = "모임 이름", example = "벗터즈")
        Long groupId,

        @Schema(description = "모임 이름", example = "벗터즈")
        String groupName,

        @Schema(description = "모임 소개", example = "STUDY")
        String groupDescription,

        @Schema(description = "모임 커버 이미지 url", example = "https://group.cover.image")
        String groupCoverImageUrl,

        @Schema(description = "모임 유형", example = "ONLINE")
        GroupType groupType,

        @Schema(description = "모임 관심사", example = "STUDY")
        GroupInterest groupInterest,

        @Schema(description = "모임에 가입된 멤버 수", example = "6")
        Long memberCount,

        @Schema(description = "모임 가입 상태", example = "APPROVED")
        JoinStatus joinStatus,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "모임 커버 이미지 식별자", example = "3")
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
