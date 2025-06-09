package team.budderz.buddyspace.infra.database.membership.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    MEMBER(1),
    SUB_LEADER(2),
    LEADER(3);

    private final int priority;
}
