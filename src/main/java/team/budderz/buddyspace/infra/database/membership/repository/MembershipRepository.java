package team.budderz.buddyspace.infra.database.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
}
