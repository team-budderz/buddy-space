package team.budderz.buddyspace.api.user.request;

public record LoginRequest(
        String email,
        String password
) {
}
