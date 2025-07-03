package team.budderz.buddyspace.api.mission.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;

@Schema(description = "미션 수정 응답 DTO")
public record UpdateMissionResponse(
        @Schema(description = "미션 제목", example = "코드카타") String title,
        @Schema(description = "미션 설명", example = "1일 1회 알고리즘 문제 풀이") String description
) {
    public static UpdateMissionResponse from(Mission mission) {
        return new UpdateMissionResponse(
                mission.getTitle(),
                mission.getDescription()
        );
    }
}
