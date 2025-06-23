package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.api.comment.response.FindsCommentResponse;

import java.time.LocalDateTime;
import java.util.List;

public record FindPostResponse(
        String userImgUrl,
        String userName,
        LocalDateTime createdAt,
        String renderedContent,
        Long commentNum,
        List<FindsCommentResponse> comments
) {
    public static FindPostResponse of(
            String userImgUrl,
            String userName,
            LocalDateTime createdAt,
            String renderedContent,
            Long commentNum,
            List<FindsCommentResponse> comments
    ) {
        return new FindPostResponse(userImgUrl, userName, createdAt, renderedContent, commentNum, comments);
    }
}

