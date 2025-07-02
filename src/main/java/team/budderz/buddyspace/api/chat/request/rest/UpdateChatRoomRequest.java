package team.budderz.buddyspace.api.chat.request.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "채팅방 수정 요청 DTO")
public record UpdateChatRoomRequest(

        @NotBlank
        @Schema(description = "채팅방 이름", example = "스터디 그룹", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @NotBlank
        @Schema(description = "채팅방 설명", example = "매주 수요일에 모여서 함께 공부해요.", requiredMode = Schema.RequiredMode.REQUIRED)
        String description
) {}
