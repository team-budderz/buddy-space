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
        Long id,
        String name,
        String description,
        Long coverAttachmentId,
        String coverImageUrl,
        GroupAccess access,
        GroupType type,
        GroupInterest interest,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String address,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Boolean isNeighborhoodAuthRequired
) {
    public static GroupResponse from(Group group, String coverImageUrl) {
        Boolean isAuthRequired = null;
        if (group.isNeighborhoodAuthRequired()) isAuthRequired = true;

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
                isAuthRequired
        );
    }
}
