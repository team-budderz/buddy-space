package team.budderz.buddyspace.infra.database.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.mission.entity.MissionPost;

public interface MissionPostRepository extends JpaRepository<MissionPost, Long> {
}
