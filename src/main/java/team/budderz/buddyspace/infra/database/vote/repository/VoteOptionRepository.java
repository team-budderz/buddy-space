package team.budderz.buddyspace.infra.database.vote.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import team.budderz.buddyspace.infra.database.vote.entity.VoteOption;

public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
	@Modifying
	@Query("DELETE FROM VoteOption vo WHERE vo.vote.id = :voteId")
	void deleteAllByVoteId(Long voteId);

	@Query("SELECT vo.id FROM VoteOption vo WHERE vo.vote.id = :voteId")
	List<Long> findAllVoteOptionIdsByVoteId(Long voteId);
}
