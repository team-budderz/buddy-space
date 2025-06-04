package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;

public record SavePostResponse(
        Long groupId,
        Long userId,
        String content,
        LocalDateTime reserveAt,
        Boolean isNotice,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static SavePostResponse from(Post post) {
        return new SavePostResponse(
                post.getGroup().getId(),
                post.getUser().getId(),
                post.getContent(),
                post.getReserveAt(),
                post.getIsNotice(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
