package team.budderz.buddyspace.api.mission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "미션 수정 요청 DTO")
public record UpdateMissionRequest(
        @NotBlank
        @Size(max = 30)
        @Schema(description = "미션 제목", example = "코드카타")
        String title,

        @NotBlank
        @Size(max = 255)
        @Schema(description = "미션 설명", example = "1일 1회 알고리즘 문제 풀이")
        String description
) {
}
