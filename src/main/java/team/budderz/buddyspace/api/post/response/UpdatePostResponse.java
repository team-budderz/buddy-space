package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;

public record UpdatePostResponse(
        String content,
        Boolean isNotice,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static UpdatePostResponse from(Post post) {
        return new UpdatePostResponse(
                post.getContent(),
                post.getIsNotice(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}