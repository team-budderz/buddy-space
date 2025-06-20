package team.budderz.buddyspace.api.missionpost.response;

import team.budderz.buddyspace.infra.database.missionpost.entity.MissionPost;

import java.time.LocalDate;

public record MissionPostDetailResponse(
        String contents,
        String missionTitle,
        String authorName,
        String authorImageUrl,
        LocalDate createdAt
) {
    public static MissionPostDetailResponse from(MissionPost missionPost, String authorImageUrl) {
        return new MissionPostDetailResponse(
                missionPost.getContents(),
                missionPost.getMission().getTitle(),
                missionPost.getAuthor().getName(),
                authorImageUrl,
                missionPost.getCreatedAt().toLocalDate()
        );
    }
}
