package team.budderz.buddyspace.api.comment.request;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank(message = "내용은 비울 수 없습니다.")
        String content
) {
}
