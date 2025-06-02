package team.budderz.buddyspace.api.user.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
