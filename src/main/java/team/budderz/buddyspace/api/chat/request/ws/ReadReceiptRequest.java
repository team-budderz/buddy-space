package team.budderz.buddyspace.api.chat.request.ws;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽음 상태 요청 DTO (WebSocket)")
public record ReadReceiptRequest(

        @Schema(description = "마지막으로 읽은 메시지 ID", example = "2003", requiredMode = Schema.RequiredMode.REQUIRED)
        Long lastReadMessageId
) {}
