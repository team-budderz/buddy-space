package team.budderz.buddyspace.api.chat.response.rest;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 수정 응답 DTO")
public record UpdateChatRoomResponse(

        @Schema(description = "채팅방 ID", example = "room-xyz123")
        String roomId,

        @Schema(description = "변경된 채팅방 이름", example = "리팩토링 팀")
        String name,

        @Schema(description = "변경된 채팅방 설명", example = "코드 리뷰 및 토론을 위한 채팅방")
        String description,

        @Schema(description = "수정 시각", example = "2025-07-02T14:00:00")
        LocalDateTime modifiedAt
) {}
