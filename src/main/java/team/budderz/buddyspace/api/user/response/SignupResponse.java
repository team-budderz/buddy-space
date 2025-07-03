package team.budderz.buddyspace.api.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.entity.UserGender;
import team.budderz.buddyspace.infra.database.user.entity.UserProvider;
import team.budderz.buddyspace.infra.database.user.entity.UserRole;

import java.time.LocalDate;

@Schema(description = "회원가입 응답 DTO")
public record SignupResponse(
        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "이메일", example = "test1@test.com")
        String email,

        @Schema(description = "생년월일", example = "2001-01-01")
        LocalDate birthDate,

        @Schema(description = "성별", example = "F")
        UserGender gender,

        @Schema(description = "주소", example = "서울 영등포구 문래동")
        String address,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "가입 경로 (LOCAL, GOOGLE)", example = "LOCAL")
        UserProvider provider,

        @Schema(description = "사용자 권한 (USER, ADMIN)", example = "USER")
        UserRole role

) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getGender(),
                user.getAddress(),
                user.getPhone(),
                user.getProvider(),
                user.getRole()
        );
    }
}
