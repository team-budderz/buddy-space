package team.budderz.buddyspace.api.chat.request.ws;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 읽음 응답 Ack 요청 DTO (WebSocket)")
public record ReadAckRequest(

        @Schema(description = "사용자 ID", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long userId,

        @Schema(description = "사용자가 마지막으로 읽은 메시지 ID", example = "105", requiredMode = Schema.RequiredMode.REQUIRED)
        Long lastReadMessageId

) {}
