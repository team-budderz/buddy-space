package team.budderz.buddyspace.api.chat.response.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 생성 응답 DTO")
public record CreateChatRoomResponse(

        @Schema(description = "생성된 채팅방 ID", example = "room-abc123")
        String roomId,

        @Schema(description = "생성된 채팅방 이름", example = "새로운 채팅방")
        String name,

        @Schema(description = "처리 결과 상태", example = "success")
        String status
) {}
