package team.budderz.buddyspace.infra.database.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Long countByGroupIdAndIsNoticeTrue(Long groupId);

    List<Post> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    List<Post> findByGroupIdAndIsNoticeTrueOrderByCreatedAtDesc(Long groupId);
}
