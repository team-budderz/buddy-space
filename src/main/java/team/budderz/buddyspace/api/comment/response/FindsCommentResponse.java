package team.budderz.buddyspace.api.comment.response;

import java.time.LocalDateTime;

public record FindsCommentResponse(
        String userImgUrl,
        String userName,
        String content,
        LocalDateTime createdAt,
        Long commentNum
) {
    public static FindsCommentResponse of(
            String userImgUrl,
            String userName,
            String content,
            LocalDateTime createdAt,
            Long commentNum
    ) {
        return new FindsCommentResponse(userImgUrl, userName, content, createdAt, commentNum);
    }
}

