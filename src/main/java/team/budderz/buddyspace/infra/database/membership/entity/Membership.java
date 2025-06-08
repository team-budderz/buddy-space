package team.budderz.buddyspace.infra.database.membership.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "memberships", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_membership_user_group",
                columnNames = {"user_id", "group_id"}
        )
})
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
    private MemberRole memberRole;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public void approve() {
        this.joinStatus = JoinStatus.APPROVED;
        this.memberRole = MemberRole.MEMBER;
        this.joinedAt = LocalDateTime.now();
    }

    public void block() {
        this.joinStatus = JoinStatus.BLOCKED;
        this.memberRole = null;
        this.joinedAt = null;
    }

    public void updateMemberRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    @Builder
    private Membership(User user,
                       Group group,
                       JoinStatus joinStatus,
                       JoinPath joinPath,
                       MemberRole memberRole,
                       LocalDateTime joinedAt) {
        this.user = user;
        this.group = group;
        this.joinStatus = joinStatus;
        this.joinPath = joinPath;
        this.memberRole = memberRole;
        this.joinedAt = joinedAt;
    }

    public static Membership fromCreator(User user, Group group) {
        return Membership.builder()
                .user(user)
                .group(group)
                .joinStatus(JoinStatus.APPROVED)
                .joinPath(JoinPath.CREATOR)
                .memberRole(MemberRole.LEADER)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public static Membership fromRequest(User user, Group group) {
        return Membership.builder()
                .user(user)
                .group(group)
                .joinStatus(JoinStatus.REQUESTED)
                .joinPath(JoinPath.REQUEST)
                .memberRole(null)
                .joinedAt(null)
                .build();
    }

    public static Membership fromInvite(User user, Group group) {
        return Membership.builder()
                .user(user)
                .group(group)
                .joinStatus(JoinStatus.APPROVED)
                .joinPath(JoinPath.INVITE)
                .memberRole(MemberRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}
