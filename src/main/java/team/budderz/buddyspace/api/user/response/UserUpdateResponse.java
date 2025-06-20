package team.budderz.buddyspace.api.user.response;

import team.budderz.buddyspace.infra.database.user.entity.User;

public record UserUpdateResponse(
        String address,
        String phone,
        String profileImageUrl
) {
    public static UserUpdateResponse from(User user, String profileImageUrl) {
        return new UserUpdateResponse(user.getAddress(), user.getPhone(), profileImageUrl);
    }
}
