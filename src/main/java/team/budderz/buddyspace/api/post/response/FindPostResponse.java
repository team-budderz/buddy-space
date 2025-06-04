package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.api.comment.response.FindCommentResponse;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record FindPostResponse(
        String userImgUrl,
        String userName,
        LocalDateTime createdAt,
        String content,
        Long commentNum,
        List<FindCommentResponse> comments
) {
    public static FindPostResponse from(Post post, List<Comment> comments) {
        return new FindPostResponse(
                post.getUser().getImageUrl(),
                post.getUser().getName(),
                post.getCreatedAt(),
                post.getContent(),
                post.getComments().stream().count(),
                post.getComments().stream()
                        .map(FindCommentResponse::from)
                        .collect(Collectors.toList())
        );
    }
}
