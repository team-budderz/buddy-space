package team.budderz.buddyspace.api.post.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "게시글 수정 요청 DTO")
public record UpdatePostRequest(
        @NotBlank(message = "내용은 비울 수 없습니다.")
        @Schema(description = "게시글 내용", example = "게시글 내용 예시입니다.")
        String content,

        @Future(message = "예약 시간은 현재보다 미래여야 합니다.")
        @Schema(description = "게시글 예약 일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime reserveAt,

        @Schema(description = "공지글 여부", example = "false")
        Boolean isNotice
) {
}
