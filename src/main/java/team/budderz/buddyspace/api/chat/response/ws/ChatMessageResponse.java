package team.budderz.buddyspace.api.chat.response.ws;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long senderId,
        String messageType,
        String content,
        String attachmentUrl,
        LocalDateTime sentAt
) {}

