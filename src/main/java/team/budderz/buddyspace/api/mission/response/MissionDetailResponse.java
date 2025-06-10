package team.budderz.buddyspace.api.mission.response;

import team.budderz.buddyspace.infra.database.mission.entity.Mission;

import java.time.LocalDate;

public record MissionDetailResponse(
        String title,
        String description,
        String startedAt,
        String endedAt,
        Integer frequency,
        String authorName,
        String authorImageUrl,
        LocalDate createdAt
) {
    public static MissionDetailResponse from(Mission mission) {
        return new MissionDetailResponse(
                mission.getTitle(),
                mission.getDescription(),
                String.valueOf(mission.getStartedAt()),
                String.valueOf(mission.getEndedAt()),
                mission.getFrequency(),
                mission.getAuthor().getName(),
                mission.getAuthor().getImageUrl(),
                mission.getCreatedAt().toLocalDate()
        );
    }
}
