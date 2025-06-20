package team.budderz.buddyspace.api.comment.response;

import team.budderz.buddyspace.infra.database.comment.entity.Comment;

import java.time.LocalDateTime;

public record FindsRecommentResponse(
        String userImgUrl,
        String userName,
        String content,
        LocalDateTime createdAt
) {
    public static FindsRecommentResponse from(Comment comment, String userImgUrl) {
        return new FindsRecommentResponse(
                userImgUrl,
                comment.getUser().getName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
