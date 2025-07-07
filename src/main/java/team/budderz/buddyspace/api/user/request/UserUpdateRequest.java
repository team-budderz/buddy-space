package team.budderz.buddyspace.api.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Schema(description = "사용자 정보 수정 요청 DTO")
public record UserUpdateRequest(
        @NotEmpty(message = "주소는 필수 입력 값입니다.")
        @Schema(description = "변경할 주소", example = "서울 강남구 역삼동")
        String address,

        @NotEmpty(message = "전화번호는 필수 입력 값입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        @Schema(description = "변경할 전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "프로필 이미지 식별자 (유지: 현재 프로필 이미지 식별자, 변경/삭제: null)", example = "12")
        Long profileAttachmentId
) {
}
