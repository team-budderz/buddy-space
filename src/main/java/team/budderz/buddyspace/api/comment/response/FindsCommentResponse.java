package team.budderz.buddyspace.api.comment.response;

import team.budderz.buddyspace.infra.database.comment.entity.Comment;

import java.time.LocalDateTime;

public record FindsCommentResponse(
        String userImgUrl,
        String userName,
        String content,
        LocalDateTime createdAt,
        Long commentNum
) {
    public static FindsCommentResponse from(Comment comment) {

        if (comment.getChildren().isEmpty()) {
            return new FindsCommentResponse(
                    comment.getUser().getImageUrl(),
                    comment.getUser().getName(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    0L
            );
        }

        return new FindsCommentResponse(
                comment.getUser().getImageUrl(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getCreatedAt(),
                (long) comment.getChildren().size()
        );
    }
}
