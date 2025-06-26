package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.api.comment.response.FindsCommentResponse;

import java.time.LocalDateTime;
import java.util.List;

public record FindPostResponse(
        Long userId,
        String userImgUrl,
        String userName,
        LocalDateTime createdAt,
        String renderedContent,
        Boolean isNotice,
        Long commentNum,
        List<FindsCommentResponse> comments
) {
    public static FindPostResponse of(
            Long userId,
            String userImgUrl,
            String userName,
            LocalDateTime createdAt,
            String renderedContent,
            Boolean isNotice,
            Long commentNum,
            List<FindsCommentResponse> comments
    ) {
        return new FindPostResponse(userId, userImgUrl, userName, createdAt, renderedContent, isNotice, commentNum, comments);
    }
}

