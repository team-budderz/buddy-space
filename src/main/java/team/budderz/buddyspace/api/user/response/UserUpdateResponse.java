package team.budderz.buddyspace.api.user.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import team.budderz.buddyspace.infra.database.user.entity.User;

public record UserUpdateResponse(
        String address,
        String phone,
        String imageUrl
) {
    public static UserUpdateResponse from(User user) {
        return new UserUpdateResponse(user.getAddress(), user.getPhone(), user.getImageUrl());
    }
}
