package team.budderz.buddyspace.api.mission.response;

import team.budderz.buddyspace.infra.database.mission.entity.Mission;

public record UpdateMissionResponse(
        String title,
        String description
) {
    public static UpdateMissionResponse from(Mission mission) {
        return new UpdateMissionResponse(
                mission.getTitle(),
                mission.getDescription()
        );
    }
}
