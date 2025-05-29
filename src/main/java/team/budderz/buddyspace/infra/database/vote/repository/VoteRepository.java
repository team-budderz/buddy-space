package team.budderz.buddyspace.infra.database.vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}
