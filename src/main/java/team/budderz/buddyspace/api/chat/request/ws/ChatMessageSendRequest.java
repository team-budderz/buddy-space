package team.budderz.buddyspace.api.chat.request.ws;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 전송 요청 DTO (WebSocket)")
public record ChatMessageSendRequest(

        @Schema(description = "채팅방 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long roomId,

        @Schema(description = "보낸 사용자 ID (서버에서 주입됨)", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long senderId,

        @Schema(description = "메시지 타입 (예: TEXT, IMAGE)", example = "TEXT", requiredMode = Schema.RequiredMode.REQUIRED)
        String messageType,

        @Schema(description = "메시지 본문 내용", example = "안녕하세요!", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @Schema(description = "첨부파일 URL (옵션)", example = "https://example.com/image.png")
        String attachmentUrl

) {}
