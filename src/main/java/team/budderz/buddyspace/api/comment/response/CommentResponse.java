package team.budderz.buddyspace.api.comment.response;

import lombok.Getter;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    private final Long groupId;
    private final Long userId;
    private final Long postId;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public CommentResponse(Comment comment) {
        this.groupId = comment.getPost().getGroup().getId();
        this.userId = comment.getUser().getId();
        this.postId = comment.getPost().getId();
        this.content = comment.getContent();
        this.createdAt =  comment.getCreatedAt();
        this.modifiedAt = comment.getModifiedAt();
    }
}
