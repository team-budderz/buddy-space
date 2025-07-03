package team.budderz.buddyspace.api.missionpost.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.missionpost.entity.MissionPost;

@Schema(description = "미션 인증 응답 DTO")
public record MissionPostResponse(
        @Schema(description = "미션 인증 식별자", example = "2")
        Long missionPostId,

        @Schema(description = "미션 인증 내용", example = "JAVA 알고리즘 문제 풀이 과정입니다.")
        String contents

) {
    public static MissionPostResponse from(MissionPost missionPost) {
        return new MissionPostResponse(
                missionPost.getId(),
                missionPost.getContents()
        );
    }
}
