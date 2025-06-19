package team.budderz.buddyspace.infra.database.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.util.Objects;

@Entity
@Table(name = "notifications")
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    public boolean doesNotBelongToUser(Long userId) {
        return !Objects.equals(this.user.getId(), userId);
    }

    public void markAsRead() {
        isRead = true;
    }
}
