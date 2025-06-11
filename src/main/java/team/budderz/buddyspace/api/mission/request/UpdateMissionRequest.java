package team.budderz.buddyspace.api.mission.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMissionRequest(
        @NotBlank
        @Size(max = 30)
        String title,
        @NotBlank
        @Size(max = 255)
        String description
) {
}
