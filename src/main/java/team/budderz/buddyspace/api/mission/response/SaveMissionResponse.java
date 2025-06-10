package team.budderz.buddyspace.api.mission.response;

import team.budderz.buddyspace.infra.database.mission.entity.Mission;

import java.time.LocalDate;

public record SaveMissionResponse(
        String title,
        String description,
        LocalDate startedAt,
        LocalDate endedAt,
        Integer frequency
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
