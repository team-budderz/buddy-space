package team.budderz.buddyspace.api.post.response;

import team.budderz.buddyspace.api.comment.response.FindsCommentResponse;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record FindPostResponse(
        String userImgUrl,
        String userName,
        LocalDateTime createdAt,
        String content,
        Long commentNum,
        List<FindsCommentResponse> comments
) {
    public static FindPostResponse from(Post post, List<Comment> comments) {
        List<Comment> topLevelComments = comments.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());

        return new FindPostResponse(
                post.getUser().getImageUrl(),
                post.getUser().getName(),
                post.getCreatedAt(),
                post.getContent(),
                (long) post.getComments().size(),
                topLevelComments.stream()
                        .map(FindsCommentResponse::from)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
    }
}
