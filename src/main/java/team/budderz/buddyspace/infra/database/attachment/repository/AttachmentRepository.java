package team.budderz.buddyspace.infra.database.attachment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Optional<Attachment> findByKey(String key);

    @Query("""
    SELECT a FROM Attachment a
    WHERE a.id NOT IN (SELECT pa.attachment.id FROM PostAttachment pa)
        AND a.id NOT IN (SELECT u.profileAttachment.id FROM User u WHERE u.profileAttachment IS NOT NULL)
        AND a.id NOT IN (SELECT g.coverAttachment.id FROM Group g WHERE g.coverAttachment IS NOT NULL)
    """)
    List<Attachment> findOrphanAttachments();
}
