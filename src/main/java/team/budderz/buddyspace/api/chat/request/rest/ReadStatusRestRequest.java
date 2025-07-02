package team.budderz.buddyspace.api.chat.request.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽음 상태 저장 요청 DTO")
public record ReadStatusRestRequest(
        @Schema(description = "마지막으로 읽은 메시지 ID", example = "1005", requiredMode = Schema.RequiredMode.REQUIRED)
        Long lastReadMessageId
) {}
