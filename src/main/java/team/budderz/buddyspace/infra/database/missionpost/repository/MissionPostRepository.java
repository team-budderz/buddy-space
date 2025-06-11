package team.budderz.buddyspace.infra.database.missionpost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;
import team.budderz.buddyspace.infra.database.missionpost.entity.MissionPost;

import java.util.List;

public interface MissionPostRepository extends JpaRepository<MissionPost, Long> {
    List<MissionPost> findAllByMission_Group_IdAndMission_Id(Long groupId, Long missionId);

}
