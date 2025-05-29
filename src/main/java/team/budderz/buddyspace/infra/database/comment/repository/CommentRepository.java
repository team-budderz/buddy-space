package team.budderz.buddyspace.infra.database.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
