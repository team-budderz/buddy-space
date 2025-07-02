package team.budderz.buddyspace.api.chat.request.ws;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 삭제 요청 DTO (WebSocket)")
public record DeleteMessageRequest(
        @Schema(description = "삭제할 메시지 ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
        Long messageId
) {}
