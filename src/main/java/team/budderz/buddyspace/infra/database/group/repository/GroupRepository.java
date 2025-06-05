package team.budderz.buddyspace.infra.database.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupQueryRepository {
}
