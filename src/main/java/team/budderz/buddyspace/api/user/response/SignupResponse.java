package team.budderz.buddyspace.api.user.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.entity.UserGender;
import team.budderz.buddyspace.infra.database.user.entity.UserProvider;
import team.budderz.buddyspace.infra.database.user.entity.UserRole;

import java.time.LocalDate;

public record SignupResponse(
        String name,
        String email,
        String password,
        LocalDate birthDate,
        UserGender gender,
        String address,
        String phone,
        UserProvider provider,
        UserRole role
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getBirthDate(),
                user.getGender(),
                user.getAddress(),
                user.getPhone(),
                user.getProvider(),
                user.getRole()
        );
    }
}
