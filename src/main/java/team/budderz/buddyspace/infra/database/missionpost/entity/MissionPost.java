package team.budderz.buddyspace.infra.database.missionpost.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "mission_posts")
public class MissionPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(nullable = false)
    private String contents;

    @Builder
    public MissionPost(String contents, Mission mission, User author) {
        this.contents = contents;
        this.mission = mission;
        this.author = author;
    }

    public void updateMissionPost(String contents) {
        this.contents = contents;
    }
}
