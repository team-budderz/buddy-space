package team.budderz.buddyspace.api.comment.response;

import team.budderz.buddyspace.infra.database.comment.entity.Comment;

import java.time.LocalDateTime;

public record FindCommentResponse(
        String userImgUrl,
        String userName,
        String content,
        LocalDateTime createdAt
) {
    public static FindCommentResponse from(Comment comment) {
        return new FindCommentResponse(
                comment.getUser().getImageUrl(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
