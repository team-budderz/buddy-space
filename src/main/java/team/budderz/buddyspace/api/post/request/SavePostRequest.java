package team.budderz.buddyspace.api.post.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record SavePostRequest(
        @NotBlank(message = "내용은 비울 수 없습니다.")
        String content,

        LocalDateTime reserveAt,
        Boolean isNotice
) {
}
