package team.budderz.buddyspace.api.membership.response;

import team.budderz.buddyspace.domain.user.provider.UserProfileImageProvider;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.util.List;

public record MembershipResponse(
        Long groupId,
        String groupName,
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
                    Long attachmentId = null;
                    if (user.getProfileAttachment() != null) attachmentId = user.getProfileAttachment().getId();
                    String url = profileImageProvider.getProfileImageUrl(attachmentId);

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
