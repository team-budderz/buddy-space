package team.budderz.buddyspace.api.comment.response;

import java.time.LocalDateTime;

public record FindsCommentResponse(
        Long commentId,
        Long userId,
        String userImgUrl,
        String userName,
        String content,
        LocalDateTime createdAt,
        Long commentNum
) {
    public static FindsCommentResponse of(
            Long commentId,
            Long userId,
            String userImgUrl,
            String userName,
            String content,
            LocalDateTime createdAt,
            Long commentNum
    ) {
        return new FindsCommentResponse(commentId, userId, userImgUrl, userName, content, createdAt, commentNum);
    }
}

