package team.budderz.buddyspace.infra.database.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;

public interface MissionRepository extends JpaRepository<Mission, Long> {
}
