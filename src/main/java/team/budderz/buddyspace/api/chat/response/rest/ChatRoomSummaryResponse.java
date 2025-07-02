package team.budderz.buddyspace.api.chat.response.rest;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 요약 응답 DTO")
public record ChatRoomSummaryResponse(

        @Schema(description = "채팅방 ID", example = "1001")
        Long roomId,

        @Schema(description = "채팅방 이름", example = "금연 챌린지")
        String name,

        @Schema(description = "최근 메시지 내용", example = "안녕하세요~")
        String lastMessage,

        @Schema(description = "최근 메시지 타입", example = "TEXT")
        String lastMessageType,

        @Schema(description = "최근 메시지 시간", example = "2025-07-02T11:30:00")
        LocalDateTime lastMessageTime,

        @Schema(description = "읽지 않은 메시지 개수", example = "3")
        long unreadCount
) {}
