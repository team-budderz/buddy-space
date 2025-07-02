package team.budderz.buddyspace.api.chat.response.rest;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "읽음 상태 조회 응답 DTO")
public record ReadStatusRestResponse(

        @Schema(description = "채팅방 ID", example = "101")
        Long roomId,

        @Schema(description = "현재 사용자의 마지막 읽은 메시지 ID", example = "1002")
        Long lastReadMessageId,

        @Schema(description = "안 읽은 메시지 수", example = "3")
        int unreadCount,

        @Schema(description = "참여자별 읽음 상태 목록")
        List<ParticipantReadStatus> participants
) {

    @Schema(description = "채팅 참여자의 읽음 상태 정보")
    public record ParticipantReadStatus(
            @Schema(description = "참여자 ID", example = "11")
            Long userId,

            @Schema(description = "해당 사용자의 마지막 읽은 메시지 ID", example = "1001")
            Long lastReadMessageId
    ) {}
}
