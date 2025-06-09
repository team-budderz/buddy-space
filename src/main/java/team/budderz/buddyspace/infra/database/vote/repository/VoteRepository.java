package team.budderz.buddyspace.infra.database.vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
	List<Vote> findByGroupIdOrderByCreatedAtDesc(Long groupId);

	void deleteAllByGroup_Id(Long groupId);
}
