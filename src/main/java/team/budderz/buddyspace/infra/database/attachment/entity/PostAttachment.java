package team.budderz.buddyspace.infra.database.attachment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.infra.database.post.entity.Post;

@Getter
@Entity
@Table(name = "post_attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id", nullable = false)
    private Attachment attachment;

    @Builder
    public PostAttachment (Post post, Attachment attachment) {
        this.post = post;
        this.attachment = attachment;
    }

    public static PostAttachment of(Post post, Attachment attachment) {
        return PostAttachment.builder()
                .post(post)
                .attachment(attachment)
                .build();
    }
}
