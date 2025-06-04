package team.budderz.buddyspace.infra.database.vote.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import team.budderz.buddyspace.infra.database.vote.entity.VoteOption;

public interface VoteOptionRepository extends JpaRepository<VoteOption, Long> {
}
