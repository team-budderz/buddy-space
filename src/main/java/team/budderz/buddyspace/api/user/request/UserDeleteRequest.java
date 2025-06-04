package team.budderz.buddyspace.api.user.request;

import jakarta.validation.constraints.NotBlank;

public record UserDeleteRequest(
        @NotBlank
        String password
) {

}
