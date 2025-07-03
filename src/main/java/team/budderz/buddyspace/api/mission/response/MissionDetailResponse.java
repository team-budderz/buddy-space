package team.budderz.buddyspace.api.mission.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;

import java.time.LocalDate;

@Schema(description = "미션 상세 조회 응답 DTO")
public record MissionDetailResponse(
        @Schema(description = "미션 식별자", example = "5")
        Long id,

        @Schema(description = "미션 제목", example = "코드카타")
        String title,

        @Schema(description = "미션 설명", example = "1일 1회 알고리즘 문제 풀이")
        String description,

        @Schema(description = "미션 시작 날짜", example = "2025-05-27")
        String startedAt,

        @Schema(description = "미션 끝나는 날짜", example = "2025-07-07")
        String endedAt,

        @Schema(description = "미션 빈도", example = "30")
        Integer frequency,

        @Schema(description = "미션 생성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "미션 생성자 프로필 이미지 url", example = "https://profile.image")
        String authorImageUrl,

        @Schema(description = "미션 생성 날짜", example = "2025-05-26")
        LocalDate createdAt

) {
    public static MissionDetailResponse from(Mission mission, String authorImageUrl) {
        return new MissionDetailResponse(
                mission.getId(),
                mission.getTitle(),
                mission.getDescription(),
                String.valueOf(mission.getStartedAt()),
                String.valueOf(mission.getEndedAt()),
                mission.getFrequency(),
                mission.getAuthor().getName(),
                authorImageUrl,
                mission.getCreatedAt().toLocalDate()
        );
    }
}
