package team.budderz.buddyspace.infra.database.vote.repository;

import java.util.List;

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

	@Modifying
	@Query("""
		DELETE FROM VoteSelection vs
		WHERE vs.user.id = :userId 
		AND vs.voteOption.id IN (
			SELECT vo.id FROM VoteOption vo WHERE vo.vote.id = :voteId
		)
	""")
	void deleteByUserIdAndVoteId(Long userId, Long voteId);
}
