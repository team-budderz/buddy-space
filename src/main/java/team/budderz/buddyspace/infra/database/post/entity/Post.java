package team.budderz.buddyspace.infra.database.post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false)
    private String content;

    private LocalDateTime reserveAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isNotice = false;

    public Post(Group group, User user, String content, LocalDateTime reserveAt, Boolean isNotice) {
        this.group = group;
        this.user = user;
        this.content = content;
        this.reserveAt = reserveAt;
        this.isNotice = isNotice;
    }
}
