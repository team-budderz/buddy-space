package team.budderz.buddyspace.infra.database.membership.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "memberships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {

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
    private MembershipRole membershipRole;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public void approve() {
        this.joinStatus = JoinStatus.APPROVED;
        this.membershipRole = MembershipRole.MEMBER;
        this.joinedAt = LocalDateTime.now();
    }

    public void block() {
        this.joinStatus = JoinStatus.BLOCKED;
        this.membershipRole = null;
        this.joinedAt = null;
    }

    private Membership(User user,
                       Group group,
                       JoinStatus joinStatus,
                       JoinPath joinPath,
                       MembershipRole membershipRole,
                       LocalDateTime joinedAt) {
        this.user = user;
        this.group = group;
        this.joinStatus = joinStatus;
        this.joinPath = joinPath;
        this.membershipRole = membershipRole;
        this.joinedAt = joinedAt;
    }

    public static Membership fromCreator(User user, Group group) {
        return new Membership(
                user,
                group,
                JoinStatus.APPROVED,
                JoinPath.CREATOR,
                MembershipRole.LEADER,
                LocalDateTime.now()
        );
    }

    public static Membership fromRequest(User user, Group group) {
        return new Membership(
                user,
                group,
                JoinStatus.REQUESTED,
                JoinPath.REQUEST,
                null,
                null
        );
    }

    public static Membership fromInvite(User user, Group group) {
        return new Membership(
                user,
                group,
                JoinStatus.APPROVED,
                JoinPath.INVITE,
                MembershipRole.MEMBER,
                LocalDateTime.now()
        );
    }
}
