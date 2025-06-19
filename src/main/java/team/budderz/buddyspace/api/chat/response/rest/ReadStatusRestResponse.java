package team.budderz.buddyspace.api.chat.response.rest;

import java.util.List;

/* HTTP API 를 위한 DTO */
public record ReadStatusRestResponse(
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
