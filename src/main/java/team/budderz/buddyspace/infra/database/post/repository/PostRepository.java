package team.budderz.buddyspace.infra.database.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // groupId로 조회한 게시글을 생성일 기준 내림차순 정렬
    List<Post> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
