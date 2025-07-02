package team.budderz.buddyspace.api.chat.request.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방에 새 사용자 초대 요청 DTO")
public record AddParticipantRequest(
        @Schema(description = "초대할 사용자 ID", example = "123") Long userId
) {}
