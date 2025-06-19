package team.budderz.buddyspace.api.chat.response.rest;

import team.budderz.buddyspace.api.chat.response.ws.ChatMessageResponse;

import java.util.List;

public record GetChatMessagesResponse(
        List<ChatMessageResponse> messages,
        int page,
        int size,
        int totalPages,
        long totalElements
) {}

