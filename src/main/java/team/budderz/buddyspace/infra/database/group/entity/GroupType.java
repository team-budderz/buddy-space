package team.budderz.buddyspace.infra.database.group.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;

/**
 * 모임 유형
 */
@Getter
@AllArgsConstructor
public enum GroupType {

    ONLINE("온라인"),
    OFFLINE("오프라인"),
    HYBRID("온/오프라인");

    private final String label;

    public static GroupType from(String label) {
        for (GroupType type : GroupType.values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        throw new GroupException(GroupErrorCode.GROUP_TYPE_NOT_FOUND);
    }
}
