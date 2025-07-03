package team.budderz.buddyspace.api.mission.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;

import java.time.LocalDate;

@Schema(description = "미션 생성 응답 DTO")
public record SaveMissionResponse(
        @Schema(description = "미션 제목", example = "코드카타") String title,
        @Schema(description = "미션 설명", example = "1일 1회 알고리즘 문제 풀이") String description,
        @Schema(description = "미션 시작 날짜", example = "2025-05-27") LocalDate startedAt,
        @Schema(description = "미션 끝나는 날짜", example = "2025-07-07") LocalDate endedAt,
        @Schema(description = "미션 빈도", example = "30") Integer frequency
) {
    public static SaveMissionResponse from(Mission mission) {
        return new SaveMissionResponse(
                mission.getTitle(),
                mission.getDescription(),
                mission.getStartedAt(),
                mission.getEndedAt(),
                mission.getFrequency()
        );
    }
}
