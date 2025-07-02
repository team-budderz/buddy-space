package team.budderz.buddyspace.api.chat.response.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.api.chat.response.ws.ChatMessageResponse;

import java.util.List;

@Schema(description = "채팅 메시지 페이지 응답 DTO")
public record GetChatMessagesResponse(

        @Schema(description = "채팅 메시지 목록")
        List<ChatMessageResponse> messages,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지당 메시지 수", example = "20")
        int size,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "전체 메시지 개수", example = "100")
        long totalElements
) {}
