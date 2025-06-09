package team.budderz.buddyspace.api.chat.response;

import java.util.List;

public record GetChatMessagesResponse(
        List<ChatMessageResponse> messages,
        int page,
        int size,
        int totalPages,
        long totalElements
) {}

