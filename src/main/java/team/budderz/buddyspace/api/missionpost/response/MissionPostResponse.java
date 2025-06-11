package team.budderz.buddyspace.api.missionpost.response;

import team.budderz.buddyspace.infra.database.missionpost.entity.MissionPost;

public record MissionPostResponse(
        Long missionPostId,
        String contents
) {
    public static MissionPostResponse from(MissionPost missionPost) {
        return new MissionPostResponse(
                missionPost.getId(),
                missionPost.getContents()
        );
    }
}
