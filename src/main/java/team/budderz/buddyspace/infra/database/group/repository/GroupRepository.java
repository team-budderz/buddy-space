package team.budderz.buddyspace.infra.database.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupQueryRepository {

    boolean existsByInviteCode(String inviteCode);

    Optional<Group> findByInviteCode(String inviteCode);

    void deleteAllByLeader_Id(Long leaderId);

    List<Group> findAllByLeader_Id(Long leaderId);
}
