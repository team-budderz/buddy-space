package team.budderz.buddyspace.api.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Schema(description = "사용자 정보 수정 응답 DTO")
public record UserUpdateResponse(
        @Schema(description = "주소", example = "서울 영등포구 문래동")
        String address,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "프로필 이미지 url", example = "https://profile.image")
        String profileImageUrl
) {
    public static UserUpdateResponse from(User user, String profileImageUrl) {
        return new UserUpdateResponse(user.getAddress(), user.getPhone(), profileImageUrl);
    }
}
