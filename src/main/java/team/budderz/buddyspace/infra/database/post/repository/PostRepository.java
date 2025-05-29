package team.budderz.buddyspace.infra.database.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
