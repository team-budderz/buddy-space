package team.budderz.buddyspace.api.chat.request.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoomType;

import java.util.List;

@Schema(description = "채팅방 생성 요청 DTO")
public record CreateChatRoomRequest(
        @Schema(description = "채팅방 이름", example = "스터디방") String name,
        @Schema(description = "채팅방 설명", example = "같이 공부하는 방") String description,
        @Schema(description = "채팅방 타입 (GROUP 또는 DIRECT)", example = "GROUP") ChatRoomType chatRoomType,
        @Schema(description = "참여할 사용자 ID 목록", example = "[1, 2, 3]") List<Long> participantIds
) {}
