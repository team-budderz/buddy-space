package team.budderz.buddyspace.infra.database.membership.entity;

import jakarta.persistence.*;
import lombok.Getter;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinStatus joinStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinPath joinPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRole membershipRole;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public void approve() {
        this.joinPath = JoinPath.REQUEST;
        this.joinStatus = JoinStatus.APPROVED;
        this.membershipRole = MembershipRole.MEMBER;
        this.joinedAt = LocalDateTime.now();
    }
}
