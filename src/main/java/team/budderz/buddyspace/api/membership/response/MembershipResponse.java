package team.budderz.buddyspace.api.membership.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.domain.user.provider.UserProfileImageProvider;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.util.List;

@Schema(description = "멤버십 응답 DTO")
public record MembershipResponse(
        @Schema(description = "모임 식별자", example = "1")
        Long groupId,

        @Schema(description = "모임 이름", example = "벗터즈")
        String groupName,

        @Schema(description = "모임 멤버 목록")
        List<MemberResponse> members

) {
    public static MembershipResponse of(
            Group group,
            List<Membership> members,
            UserProfileImageProvider profileImageProvider
    ) {
        List<MemberResponse> result = members.stream()
                .map(membership -> {
                    User user = membership.getUser();
                    String url = profileImageProvider.getProfileImageUrl(user);

                    return MemberResponse.of(
                            user.getId(),
                            user.getName(),
                            url,
                            membership.getMemberRole(),
                            membership.getJoinStatus(),
                            membership.getJoinPath(),
                            membership.getJoinedAt()
                    );
                })
                .toList();

        return new MembershipResponse(
                group.getId(),
                group.getName(),
                result
        );
    }
}
