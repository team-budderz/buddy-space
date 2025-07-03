package team.budderz.buddyspace.api.group.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.group.entity.Group;

@Schema(description = "모임 초대 링크 응답 DTO")
public record GroupInviteResponse(
        @Schema(description = "모임 식별자", example = "13")
        Long groupId,

        @Schema(description = "모임 이름", example = "벗터즈")
        String groupName,

        @Schema(description = "모임 소개", example = "함께 공부하는 모임입니다.")
        String groupDescription,

        @Schema(description = "모임 초대 링크", example = "https://budderz.co.kr/invite?code=QIBqqS4q6UWRPFvA")
        String inviteLink,

        @Schema(description = "모임 초대 코드", example = "QIBqqS4q6UWRPFvA")
        String code

) {
    public static GroupInviteResponse of(Group group, String inviteLink) {
        return new GroupInviteResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                inviteLink,
                group.getInviteCode()
        );
    }
}
