package team.budderz.buddyspace.domain.post.event;

import lombok.Getter;
import team.budderz.buddyspace.infra.database.post.entity.Post;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
public class PostEvent {
    private final Post post;
    private final User writer;

    public PostEvent(Post post, User writer) {
        this.post = post;
        this.writer = writer;
    }
}
