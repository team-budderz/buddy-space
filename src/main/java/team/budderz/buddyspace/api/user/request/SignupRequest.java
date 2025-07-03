package team.budderz.buddyspace.api.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import team.budderz.buddyspace.infra.database.user.entity.UserGender;

import java.time.LocalDate;

@Schema(description = "회원가입 요청 DTO")
public record SignupRequest(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 한글 또는 영문만 입력 가능합니다.")
        @Schema(description = "이름", example = "홍길동")
        String name,

        @NotBlank
        @Pattern(regexp = "^[\\w!#$%&'*+/=?`{|}~^.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
        @Schema(description = "이메일", example = "test1@test.com")
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "비밀번호 형식이 올바르지 않습니다. 8자 이상, 대소문자 포함, 숫자 및 특수문자(@$!%*?&#) 포함")
        @Schema(description = "비밀번호", example = "Password1@")
        String password,

        @NotNull
        @Schema(description = "생년월일", example = "2001-01-01")
        LocalDate birthDate,

        @NotNull
        @Schema(description = "성별 (F, M, UNKNOWN)", example = "F")
        UserGender gender,

        @NotBlank
        @Schema(description = "주소", example = "서울 영등포구 문래동")
        String address,

        @NotBlank
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone
) {
}
