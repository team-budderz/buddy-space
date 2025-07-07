package team.budderz.buddyspace.api.comment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 생성 요청 DTO")
public record CommentRequest(
        @NotBlank(message = "내용은 비울 수 없습니다.")
        @Schema(description = "댓글 내용", example = "댓글 내용 예시입니다.")
        String content
) {
}
