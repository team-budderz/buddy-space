package team.budderz.buddyspace.infra.database.notification.entity;

public enum NotificationType {
    // 일반 알람
    POST,
    COMMENT,
    REPLY,
    NOTICE,

    // 초대 관련
    GROUP_JOIN_REQUEST,
    GROUP_JOIN_APPROVED
}
