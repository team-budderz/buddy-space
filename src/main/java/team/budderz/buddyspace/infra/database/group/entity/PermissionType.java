package team.budderz.buddyspace.infra.database.group.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static team.budderz.buddyspace.infra.database.group.entity.PermissionAction.*;
import static team.budderz.buddyspace.infra.database.group.entity.ContentType.*;

@Getter
@RequiredArgsConstructor
public enum PermissionType {
    CREATE_POST(CREATE, POST),
    DELETE_POST(DELETE, POST),

    CREATE_SCHEDULE(CREATE, SCHEDULE),
    DELETE_SCHEDULE(DELETE, SCHEDULE),

    CREATE_MISSION(CREATE, MISSION),
    DELETE_MISSION(DELETE, MISSION),

    CREATE_VOTE(CREATE, VOTE),
    DELETE_VOTE(DELETE, VOTE),

    CREATE_DIRECT_CHAT_ROOM(CREATE, DIRECT_CHAT_ROOM),

    CREATE_INVITE_LINK(CREATE, INVITE_LINK),

    INVITE_CHAT_PARTICIPANT(CREATE, CHAT_PARTICIPANT),
    KICK_CHAT_PARTICIPANT(DELETE, CHAT_PARTICIPANT);

    private final PermissionAction action;
    private final ContentType content;
}
