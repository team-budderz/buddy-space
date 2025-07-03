package team.budderz.buddyspace.api.mission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "미션 생성 요청 DTO")
public record SaveMissionRequest(
        @NotBlank
        @Size(max = 30)
        @Schema(description = "미션 제목", example = "코드카타")
        String title,

        @NotBlank
        @Size(max = 255)
        @Schema(description = "미션 설명", example = "1일 1회 알고리즘 문제 풀이")
        String description,

        @NotNull
        @Schema(description = "미션 시작 날짜", example = "2025-05-27")
        LocalDate startedAt,

        @NotNull
        @Schema(description = "미션 끝나는 날짜", example = "2025-07-07")
        LocalDate endedAt,

        @NotNull
        @Min(value = 1, message = "미션 빈도는 1회 이상부터 설정 가능합니다.")
        @Schema(description = "미션 빈도", example = "30")
        Integer frequency
) {
}
