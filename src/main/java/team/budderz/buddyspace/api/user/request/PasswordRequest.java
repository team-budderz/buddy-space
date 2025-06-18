package team.budderz.buddyspace.api.user.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordRequest(
        @NotBlank
        String password
) {
}
