package team.budderz.buddyspace.api.post.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostRequest(
        @NotBlank(message = "내용은 비울 수 없습니다.")
        String content,

        Boolean isNotice
) {
}
