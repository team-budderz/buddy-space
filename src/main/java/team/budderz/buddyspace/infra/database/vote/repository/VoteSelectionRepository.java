package team.budderz.buddyspace.infra.database.vote.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Query("""
		SELECT new map(vs.voteOption.id as optionId, u.name as userName)
		FROM VoteSelection vs
		JOIN vs.user u
		WHERE vs.voteOption.vote.id = :voteId
	""")
	List<Map<String, Object>> findVoterNamesByVoteId(Long voteId);

	default Map<Long, List<String>> findVoterNamesGroupedByOptionId(Long voteId) {
		List<Map<String, Object>> raw = findVoterNamesByVoteId(voteId);
		Map<Long, List<String>> result = new HashMap<>();
		for (Map<String, Object> row : raw) {
			Long optionId = (Long) row.get("optionId");
			String userName = (String) row.get("userName");
			result.computeIfAbsent(optionId, k -> new ArrayList<>()).add(userName);
		}
		return result;
	}
}
