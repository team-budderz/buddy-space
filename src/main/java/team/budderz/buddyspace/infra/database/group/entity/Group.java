package team.budderz.buddyspace.infra.database.group.entity;

import jakarta.persistence.*;
import lombok.Getter;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
@Entity
@Table(name = "groups")
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interest;

    @Column(name = "is_neighborhood_auth_required")
    private boolean isNeighborhoodAuthRequired;

    @Column(name = "is_mission_group")
    private boolean isMissionGroup;

    @Column(name = "is_neighborhood_group")
    private boolean isNeighborhoodGroup;

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @ManyToOne
    @JoinColumn(name = "neighborhood_id")
    private Neighborhood neighborhood;
}
