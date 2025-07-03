package team.budderz.buddyspace.api.group.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;

/**
 * 모임 생성/수정 응답 DTO
 */
@Schema(description = "모임 응답 DTO")
public record GroupResponse(
        @Schema(description = "모임 식별자", example = "1")
        Long id,

        @Schema(description = "모임 이름", example = "벗터즈")
        String name,

        @Schema(description = "모임 소개", example = "함께 공부하는 모임입니다.")
        String description,

        @Schema(description = "모임 커버 이미지 식별자", example = "13")
        Long coverAttachmentId,

        @Schema(description = "모임 커버 이미지 url", example = "https://group.cover.image")
        String coverImageUrl,

        @Schema(description = "모임 공개 여부", example = "PUBLIC")
        GroupAccess access,

        @Schema(description = "모임 유형", example = "ONLINE")
        GroupType type,

        @Schema(description = "모임 관심사", example = "STUDY")
        GroupInterest interest,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "모임 동네 (오프라인 모임일 경우에만 포함)", example = "서울 영등포구 문래동")
        String address,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "동네 미인증 사용자 가입 제한 여부 (오프라인 모임일 경우에만 포함)", example = "false")
        Boolean isNeighborhoodAuthRequired

) {
    public static GroupResponse from(Group group, String coverImageUrl) {
        Long coverAttachmentId = null;
        if (group.getCoverAttachment() != null) coverAttachmentId = group.getCoverAttachment().getId();

        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                coverAttachmentId,
                coverImageUrl,
                group.getAccess(),
                group.getType(),
                group.getInterest(),
                group.getAddress(),
                group.getIsNeighborhoodAuthRequired()
        );
    }
}
