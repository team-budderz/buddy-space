package team.budderz.buddyspace.api.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 인증 요청 DTO")
public record PasswordRequest(
        @NotBlank
        @Schema(description = "사용자 비밀번호", example = "Password1@")
        String password
) {
}
