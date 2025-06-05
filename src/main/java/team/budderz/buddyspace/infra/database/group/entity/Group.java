package team.budderz.buddyspace.infra.database.group.entity;

import io.micrometer.common.util.StringUtils;
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

    @Column(columnDefinition = "TEXT")
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupAccess access;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupInterest interest;

    @Column(name = "invite_link")
    private String inviteLink;

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
    public Group(String name, String coverImageUrl, GroupAccess access, GroupType type, GroupInterest interest, User leader) {
        this.name = name;
        this.coverImageUrl = coverImageUrl;
        this.access = access;
        this.type = type;
        this.interest = interest;
        this.leader = leader;
    }

    public void updateGroupInfo(String name,
                                String description,
                                String coverImageUrl,
                                GroupAccess access,
                                GroupType type,
                                GroupInterest interest) {

        if (!StringUtils.isBlank(name)) this.name = name;
        if (!StringUtils.isBlank(description)) this.description = description;
        if (!StringUtils.isBlank(coverImageUrl)) this.coverImageUrl = coverImageUrl;
        if (access != null) this.access = access;
        if (type != null) this.type = type;
        if (interest != null) this.interest = interest;
    }
}
