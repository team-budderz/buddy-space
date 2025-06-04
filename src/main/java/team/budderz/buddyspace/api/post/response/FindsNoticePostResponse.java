package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.infra.database.post.entity.Post;

public record FindsNoticePostResponse(
        String content
) {
    public static FindsNoticePostResponse from(Post post) {
        String content = post.getContent();
        String shortcontent =
                content.length() > 20 ? content.substring(0, 20) + "···" : content;

        return new FindsNoticePostResponse(
                shortcontent
        );
    }
}
