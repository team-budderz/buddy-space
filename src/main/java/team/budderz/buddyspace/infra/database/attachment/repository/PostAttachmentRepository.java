package team.budderz.buddyspace.infra.database.attachment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.attachment.entity.PostAttachment;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.util.List;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

    void deleteAllByPost(Post post);

    List<PostAttachment> findAllByPost(Post post);

    List<PostAttachment> findAllByPostIn(List<Post> posts);
}
