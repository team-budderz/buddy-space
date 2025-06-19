package team.budderz.buddyspace.api.notification.response;

public record NotificationArgs(
        String senderName,
        String groupName,
        String content,
        Long targetId
) {
}
