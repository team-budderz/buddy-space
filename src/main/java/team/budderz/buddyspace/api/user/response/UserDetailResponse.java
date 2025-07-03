package team.budderz.buddyspace.api.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.entity.UserGender;
import team.budderz.buddyspace.infra.database.user.entity.UserProvider;

import java.time.LocalDate;

@Schema(description = "사용자 상세 조회 응답 DTO")
public record UserDetailResponse(
        @Schema(description = "식별자", example = "1")
        Long id,

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

        @Schema(description = "동네 인증 여부", example = "false")
        boolean hasNeighborhood,

        @Schema(description = "프로필 이미지 식별자", example = "11")
        Long profileAttachmentId,

        @Schema(description = "프로필 이미지 url", example = "https://profile.image")
        String profileImageUrl
) {
    public static UserDetailResponse from(User user, String profileImageUrl) {
        Long profileAttachmentId = null;
        if (user.getProfileAttachment() != null) profileAttachmentId = user.getProfileAttachment().getId();

        return new UserDetailResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getGender(),
                user.getAddress(),
                user.getPhone(),
                user.getProvider(),
                user.getNeighborhood() != null,
                profileAttachmentId,
                profileImageUrl
        );
    }
}
