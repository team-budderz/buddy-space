package team.budderz.buddyspace.domain.comment.event;

import lombok.Getter;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
public class ReplyEvent {
    private final Comment recomment;
    private final User writer;

    public ReplyEvent(Comment recomment, User writer) {
        this.recomment = recomment;
        this.writer = writer;
    }
}

