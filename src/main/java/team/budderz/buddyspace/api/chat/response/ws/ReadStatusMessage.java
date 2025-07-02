package team.budderz.buddyspace.api.chat.response.ws;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "읽음 상태 전체 동기화 메시지")
public record ReadStatusMessage(

        @Schema(description = "채팅방 ID", example = "123")
        Long roomId,

        @Schema(description = "참여자들의 읽음 상태 목록")
        List<Participant> participants
) {
    @Schema(description = "읽음 상태를 나타내는 참여자 정보")
    public record Participant(
            @Schema(description = "참여자 ID", example = "42")
            Long userId,

            @Schema(description = "참여자의 마지막 읽은 메시지 ID", example = "1002")
            Long lastReadMessageId
    ) {}
}
