package team.budderz.buddyspace.api.post.response;

import lombok.Getter;
import team.budderz.buddyspace.infra.database.post.entity.Post;

@Getter
public class UpdatePostResponse {

    private final String content;
    private final Boolean isNotice;

    public UpdatePostResponse(Post post) {
        this.content = post.getContent();
        this.isNotice = post.getIsNotice();
    }
}