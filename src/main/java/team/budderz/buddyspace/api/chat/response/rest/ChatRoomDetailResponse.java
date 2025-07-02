package team.budderz.buddyspace.api.chat.response.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoomType;

import java.time.LocalDateTime;

@Schema(description = "채팅방 상세 응답 DTO")
public record ChatRoomDetailResponse(

        @Schema(description = "채팅방 ID", example = "room-1234")
        String roomId,

        @Schema(description = "채팅방 이름", example = "스터디 그룹")
        String name,

        @Schema(description = "채팅방 설명", example = "매일 오전 10시 스터디 채널")
        String description,

        @Schema(description = "채팅방 타입 (GROUP / DIRECT)", example = "GROUP")
        ChatRoomType type,

        @Schema(description = "채팅방 생성자 ID", example = "42")
        Long createdBy,

        @Schema(description = "채팅방 생성 시각", example = "2025-07-02T10:15:30")
        LocalDateTime createdAt,

        @Schema(description = "현재 참여자 수", example = "5")
        int participantCount
) {}
