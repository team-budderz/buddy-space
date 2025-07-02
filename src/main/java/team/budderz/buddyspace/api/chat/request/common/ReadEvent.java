package team.budderz.buddyspace.api.chat.request.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽음 이벤트 DTO")
public record ReadEvent(
        @Schema(description = "사용자 ID", example = "42") Long userId,
        @Schema(description = "마지막으로 읽은 메시지 ID", example = "1005") Long lastReadMessageId
) {}
