package team.budderz.buddyspace.api.missionpost.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MissionPostRequest(
        @NotBlank
        @Size(max = 255)
        String contents
) {
}
