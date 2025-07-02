package team.budderz.buddyspace.api.chat.response.ws;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 읽음 응답 DTO")
public record ReadReceiptResponse(

        @Schema(description = "사용자 ID", example = "42")
        Long userId,

        @Schema(description = "마지막으로 읽은 메시지 ID", example = "1005")
        Long lastReadMessageId
) {}
