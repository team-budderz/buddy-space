package team.budderz.buddyspace.infra.database.vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import team.budderz.buddyspace.infra.database.vote.entity.VoteSelection;

public interface VoteSelectionRepository extends JpaRepository<VoteSelection, Long> {
	@Modifying
	@Query("""
		DELETE FROM VoteSelection vs
		WHERE vs.voteOption.id IN (
			SELECT vo.id FROM VoteOption vo WHERE vo.vote.id = :voteId
		)
	""")
	void deleteAllByVoteOptionIn(Long voteId);
}
