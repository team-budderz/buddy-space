package team.budderz.buddyspace.infra.database.attachment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.attachment.entity.PostAttachment;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.util.List;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

    void deleteAllByPost(Post post);

    boolean existsAllByAttachment(Attachment attachment);

    void deleteByAttachment(Attachment attachment);

    List<PostAttachment> findAllByPost(Post post);

    List<PostAttachment> findAllByPostIn(List<Post> posts);
}
