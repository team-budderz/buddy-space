package team.budderz.buddyspace.api.post.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record SavePostRequest(
        @NotBlank(message = "내용은 비울 수 없습니다.")
        String content,

        @Future(message = "예약 시간은 현재보다 미래여야 합니다.")
        LocalDateTime reserveAt,
        Boolean isNotice
) {
}
