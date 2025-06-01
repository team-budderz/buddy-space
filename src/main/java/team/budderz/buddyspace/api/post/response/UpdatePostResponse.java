package team.budderz.buddyspace.api.post.response;

import lombok.Getter;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;

@Getter
public class UpdatePostResponse {

    private final String content;
    private final Boolean isNotice;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public UpdatePostResponse(Post post) {
        this.content = post.getContent();
        this.isNotice = post.getIsNotice();
        this.createdAt =  post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
    }
}