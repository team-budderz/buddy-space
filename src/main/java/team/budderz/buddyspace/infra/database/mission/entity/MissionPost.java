package team.budderz.buddyspace.infra.database.mission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "mission_posts")
public class MissionPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String contents;

    public MissionPost(String contents) {
        this.contents = contents;
    }
}
