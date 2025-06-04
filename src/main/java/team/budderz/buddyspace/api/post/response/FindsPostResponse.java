package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;

public record FindsPostResponse(
        String userImgUrl,
        String userName,
        LocalDateTime createdAt,
        String content,
        Long commentsNum
) {
    public static FindsPostResponse from(Post post) {
        return new FindsPostResponse(
                post.getUser().getImageUrl(),
                post.getUser().getName(),
                post.getCreatedAt(),
                post.getContent(),
                post.getComments().stream().count()
        );
    }
}
