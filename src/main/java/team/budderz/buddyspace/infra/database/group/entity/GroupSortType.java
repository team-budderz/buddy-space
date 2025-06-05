package team.budderz.buddyspace.infra.database.group.entity;

import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;

import java.util.Arrays;

public enum GroupSortType {
    POPULAR,
    LATEST;

    public static GroupSortType from(String value) {
        if (value == null) {
            throw new GroupException(GroupErrorCode.INVALID_GROUP_SORT_TYPE);
        }
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new GroupException(GroupErrorCode.INVALID_GROUP_SORT_TYPE));
    }
}
