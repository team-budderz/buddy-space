package team.budderz.buddyspace.infra.database.group.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;

import static team.budderz.buddyspace.domain.group.constant.GroupDefaults.*;

/**
 * 모임 유형
 */
@Getter
@AllArgsConstructor
public enum GroupType {

    ONLINE("온라인", DEFAULT_ONLINE_GROUP_COVER_IMAGE_URL),
    OFFLINE("오프라인", DEFAULT_OFFLINE_GROUP_COVER_IMAGE_URL),
    HYBRID("온/오프라인", DEFAULT_HYBRID_GROUP_COVER_IMAGE_URL);

    private final String label;
    private final String defaultCoverImageUrl;

    public static GroupType from(String label) {
        for (GroupType type : GroupType.values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        throw new GroupException(GroupErrorCode.GROUP_TYPE_NOT_FOUND);
    }
}
