package team.budderz.buddyspace.api.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
        @NotEmpty(message = "주소는 필수 입력 값입니다.")
        String address,

        @NotEmpty(message = "전화번호는 필수 입력 값입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phone,

        Long profileAttachmentId
) {
}
