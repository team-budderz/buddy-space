package team.budderz.buddyspace.api.mission.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SaveMissionRequest(
        @NotBlank
        @Size(max = 30)
        String title,
        @NotBlank
        @Size(max = 255)
        String description,
        @NotNull
        LocalDate startedAt,
        @NotNull
        LocalDate endedAt,
        @NotNull
        @Min(value = 1, message = "미션 빈도는 1회 이상부터 설정 가능합니다.")
        Integer frequency
) {
}
