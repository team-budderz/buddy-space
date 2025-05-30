package team.budderz.buddyspace.infra.database.group.entity;

import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;

/**
 * 모임 관심사
 */
public enum GroupInterest {
    HOBBY("취미"),
    FAMILY("가족"),
    SCHOOL("학교"),
    BUSINESS("업무"),
    EXERCISE("운동"),
    GAME("게임"),
    STUDY("스터디"),
    FAN("팬"),
    OTHER("기타");

    private final String label;

    GroupInterest(String label) {
        this.label = label;
    }

    public static GroupInterest from(String label) {
        for (GroupInterest interest : GroupInterest.values()) {
            if (interest.label.equals(label)) {
                return interest;
            }
        }
        throw new GroupException(GroupErrorCode.INTEREST_NOT_FOUND);
    }
}
