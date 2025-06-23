package team.budderz.buddyspace.domain.comment.event;

import lombok.Getter;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
public class CommentEvent {
    private final Comment comment;
    private final User writer;

    public CommentEvent(Comment comment, User writer) {
        this.comment = comment;
        this.writer = writer;
    }
}

