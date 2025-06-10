package team.budderz.buddyspace.infra.database.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    Long countByGroupIdAndIsNoticeTrue(Long groupId);

    List<Post> findByGroupIdAndIsNoticeTrueOrderByCreatedAtDesc(Long groupId);

    void deleteAllByGroup_Id(Long groupId);
}
