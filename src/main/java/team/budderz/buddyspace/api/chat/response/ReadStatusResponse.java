package team.budderz.buddyspace.api.chat.response;

import java.util.List;

public record ReadStatusResponse(
        Long roomId,
        Long lastReadMessageId,
        int unreadCount,
        List<ParticipantReadStatus> participants
) {
    public record ParticipantReadStatus(
            Long userId,
            Long lastReadMessageId
    ){}
}
