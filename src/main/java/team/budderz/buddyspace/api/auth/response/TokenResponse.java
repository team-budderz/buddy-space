package team.budderz.buddyspace.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 응답 DTO")
public record TokenResponse(
        @Schema(description = "발급된 액세스 토큰", example = "token_value")
        String accessToken
) {
}
