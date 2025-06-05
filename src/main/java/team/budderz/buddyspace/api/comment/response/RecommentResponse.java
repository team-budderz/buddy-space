package team.budderz.buddyspace.api.comment.response;

import team.budderz.buddyspace.infra.database.comment.entity.Comment;

import java.time.LocalDateTime;

public record RecommentResponse(
        Long groupId,
        Long userId,
        Long postId,
        Long commentId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static RecommentResponse from(Comment comment) {
        return new RecommentResponse(
                comment.getPost().getGroup().getId(),
                comment.getUser().getId(),
                comment.getPost().getId(),
                comment.getParent().getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getModifiedAt()
        );
    }
}
