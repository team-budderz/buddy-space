package team.budderz.buddyspace.api.comment.response;

import team.budderz.buddyspace.infra.database.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long groupId,
        Long userId,
        Long postId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getPost().getGroup().getId(),
                comment.getUser().getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getModifiedAt()
        );
    }
}
