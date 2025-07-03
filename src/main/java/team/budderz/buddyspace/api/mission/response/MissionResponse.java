package team.budderz.buddyspace.api.mission.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Schema(description = "미션 응답 DTO")
public record MissionResponse(
        @Schema(description = "미션 식별자", example = "3")
        Long missionId,

        @Schema(description = "미션 제목", example = "코드카타") String title,
        @Schema(description = "미션 설명", example = "1일 1회 알고리즘 문제 풀이") String description,
        @Schema(description = "미션 시작 날짜", example = "2025-05-27") String startedAt,
        @Schema(description = "미션 끝나는 날짜", example = "2025-07-07") String endedAt,
        @Schema(description = "미션 빈도", example = "30") Integer frequency,
        @Schema(description = "미션 진행된 일수", example = "2") int progressDay,
        @Schema(description = "미션 생성자 이름", example = "홍길동") String authorName
) {
    public static MissionResponse from(Mission mission) {
        LocalDate today = LocalDate.now();
        int progressDay = (int) ChronoUnit.DAYS.between(mission.getStartedAt(), today) + 1;

        return new MissionResponse(
                mission.getId(),
                mission.getTitle(),
                mission.getDescription(),
                String.valueOf(mission.getStartedAt()),
                String.valueOf(mission.getEndedAt()),
                mission.getFrequency(),
                progressDay,
                mission.getAuthor().getName()
        );
    }
}
