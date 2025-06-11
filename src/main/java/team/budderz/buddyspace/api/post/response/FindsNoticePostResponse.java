package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.infra.database.post.entity.Post;

public record FindsNoticePostResponse(
        String content
) {
    public static FindsNoticePostResponse from(Post post) {
        return new FindsNoticePostResponse(
                post.getContent()
        );
    }
}
