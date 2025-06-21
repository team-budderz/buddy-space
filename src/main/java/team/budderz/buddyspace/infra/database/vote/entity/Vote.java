package team.budderz.buddyspace.infra.database.vote.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "votes")
@ToString(exclude = {"author", "group", "options"})
public class Vote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean isAnonymous;

    @Column(nullable = false)
    private boolean isClosed;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.PERSIST)
    private List<VoteOption> options;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "group_id", nullable = false)
   private Group group;

   @Builder
    public Vote(String title, boolean isAnonymous, List<String> options, User author, Group group) {
        this.title = title;
        this.isAnonymous = isAnonymous;
        this.isClosed = false;
        this.options = new ArrayList<>();
        for (String optionName : options) {
            addOption(optionName);
        }
        this.author = author;
        this.group = group;
    }

    private void addOption(String optionName) {
        VoteOption voteOption = VoteOption.builder()
            .content(optionName)
            .vote(this)
            .build();
        options.add(voteOption);
    }

    public void update(String title, boolean isAnonymous, List<String> options) {
        this.title = title;
        this.isAnonymous = isAnonymous;
        this.options.clear();
        for (String optionName : options) {
            addOption(optionName);
        }
    }

    public void close() {
       this.isClosed = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Vote vote) {
            return id != null && id.equals(vote.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
