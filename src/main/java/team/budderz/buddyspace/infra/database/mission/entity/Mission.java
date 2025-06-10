package team.budderz.buddyspace.infra.database.mission.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "missions")
public class Mission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate startedAt;

    @Column(nullable = false)
    private LocalDate endedAt;

    @Column(nullable = false)
    private Integer frequency;

    @Builder
    public Mission(String title, String description, LocalDate startedAt, LocalDate endedAt, Integer frequency, User author, Group group) {
        this.title = title;
        this.description = description;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.frequency = frequency;
        this.author = author;
        this.group = group;
    }

    public void updateMission(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
