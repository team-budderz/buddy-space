package team.budderz.buddyspace.infra.database.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
@Entity
@Table(name = "groups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupAccess access;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupInterest interest;

    @Column(name = "is_neighborhood_auth_required")
    private boolean isNeighborhoodAuthRequired;

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @ManyToOne
    @JoinColumn(name = "neighborhood_id")
    private Neighborhood neighborhood;

    /**
     * 모임 생성용 생성자
     */
    public Group(String name, GroupAccess access, GroupType type, GroupInterest interest, User leader) {
        this.name = name;
        this.access = access;
        this.type = type;
        this.interest = interest;
        this.leader = leader;
    }
}
