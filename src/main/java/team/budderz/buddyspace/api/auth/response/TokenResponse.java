package team.budderz.buddyspace.api.auth.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
