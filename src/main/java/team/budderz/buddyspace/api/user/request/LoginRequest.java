package team.budderz.buddyspace.api.user.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청 DTO")
public record LoginRequest(
        @Schema(description = "사용자 이메일", example = "test1@test.com")
        String email,

        @Schema(description = "사용자 비밀번호", example = "Password1@")
        String password
) {
}
