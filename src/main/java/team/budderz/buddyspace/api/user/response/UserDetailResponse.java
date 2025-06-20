package team.budderz.buddyspace.api.user.response;

import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.entity.UserGender;

import java.time.LocalDate;

public record UserDetailResponse(
        Long id,
        String name,
        String email,
        LocalDate birthDate,
        UserGender gender,
        String address,
        String phone,
        String profileImageUrl
) {
    public static UserDetailResponse from(User user, String profileImageUrl) {
        return new UserDetailResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getGender(),
                user.getAddress(),
                user.getPhone(),
                profileImageUrl
        );
    }
}
