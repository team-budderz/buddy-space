package team.budderz.buddyspace.api.missionpost.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.missionpost.entity.MissionPost;

import java.time.LocalDate;

@Schema(description = "미션 인증 상세 조회 응답 DTO")
public record MissionPostDetailResponse(
        @Schema(description = "미션 인증 내용", example = "JAVA 알고리즘 문제 풀이 과정입니다.")
        String contents,

        @Schema(description = "미션 제목", example = "코드카타")
        String missionTitle,

        @Schema(description = "작성자 이름", example = "김팀원")
        String authorName,

        @Schema(description = "작성자 프로필 이미지 url", example = "https://profile.image")
        String authorImageUrl,

        @Schema(description = "생성일자", example = "2025-05-15")
        LocalDate createdAt

) {
    public static MissionPostDetailResponse from(MissionPost missionPost, String authorImageUrl) {
        return new MissionPostDetailResponse(
                missionPost.getContents(),
                missionPost.getMission().getTitle(),
                missionPost.getAuthor().getName(),
                authorImageUrl,
                missionPost.getCreatedAt().toLocalDate()
        );
    }
}
