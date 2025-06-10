package team.budderz.buddyspace.infra.database.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    void deleteAllByGroup_Id(Long groupId);

    List<Mission> findAllByGroup_Id(Long groupId);

    long countMissionsByGroup_Id(Long groupId);
}
