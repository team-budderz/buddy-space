package team.budderz.buddyspace.api.chat.response.ws;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅 메시지 응답 DTO")
public record ChatMessageResponse(

        @Schema(description = "메시지 ID", example = "1001")
        Long messageId,

        @Schema(description = "보낸 사용자 ID", example = "42")
        Long senderId,

        @Schema(description = "메시지 타입 (TEXT, IMAGE 등)", example = "TEXT")
        String messageType,

        @Schema(description = "메시지 본문", example = "안녕하세요!")
        String content,

        @Schema(description = "첨부파일 URL", example = "https://example.com/image.png")
        String attachmentUrl,

        @Schema(description = "메시지 전송 시각", example = "2025-07-02T15:30:00")
        LocalDateTime sentAt
) {}
