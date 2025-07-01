package team.budderz.buddyspace.infra.database.vote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import team.budderz.buddyspace.infra.database.vote.entity.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {
	@Query("""
		SELECT v
		FROM Vote v JOIN FETCH v.author
		WHERE v.group.id = :groupId
		ORDER BY v.createdAt desc
	""")
	List<Vote> findByGroupIdOrderByCreatedAtDesc(Long groupId);

	void deleteAllByGroup_Id(Long groupId);

	@Query("""
		SELECT v
		FROM Vote v JOIN FETCH v.author
		WHERE v.id = :voteId
	""")
	Optional<Vote> findById(Long voteId);
}
