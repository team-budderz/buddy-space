package team.budderz.buddyspace.api.mission.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateMissionRequest(
        @NotBlank
        String title,
        @NotBlank
        String description
) {
}
