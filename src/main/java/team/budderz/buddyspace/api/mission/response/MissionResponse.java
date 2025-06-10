package team.budderz.buddyspace.api.mission.response;

import team.budderz.buddyspace.infra.database.mission.entity.Mission;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record MissionResponse(
        Long missionId,
        String title,
        String description,
        String startedAt,
        String endedAt,
        Integer frequency,
        int progressDay,
        String authorName
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
