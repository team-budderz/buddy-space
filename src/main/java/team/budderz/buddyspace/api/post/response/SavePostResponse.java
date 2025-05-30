package team.budderz.buddyspace.api.post.response;

import lombok.Getter;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;

@Getter
public class SavePostResponse {

    private final Long groupId;
    private final Long userId;
    private final String content;
    private final LocalDateTime reserveAt;
    private final Boolean isNotice;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public SavePostResponse(Post post) {
        this.groupId = post.getGroup().getId();
        this.userId = post.getUser().getId();
        this.content = post.getContent();
        this.reserveAt = post.getReserveAt();
        this.isNotice = post.getIsNotice();
        this.createdAt =  post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
    }
}
