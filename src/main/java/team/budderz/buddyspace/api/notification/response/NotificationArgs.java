package team.budderz.buddyspace.api.notification.response;

public record NotificationArgs(
        String senderName,
        String groupName,
        String content,
        Long memberId,
        Long groupId,
        Long postId,
        Long commentId
) {
}
