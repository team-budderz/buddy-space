package team.budderz.buddyspace.infra.database.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

@Getter
@Entity
@Table(name = "group_permissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType type;

    private GroupPermission(Group group, MemberRole role, PermissionType type) {
        this.group = group;
        this.role = role;
        this.type = type;
    }

    public static GroupPermission of(Group group, MemberRole role, PermissionType type) {
        return new GroupPermission(group, role, type);
    }
}
