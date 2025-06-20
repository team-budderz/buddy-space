package team.budderz.buddyspace.infra.database.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    void deleteAllByUser_Id(Long userId);

    void deleteAllByGroup_Id(Long groupId);

    boolean existsByGroup_IdAndMemberRoleNot(Long groupId, MemberRole role);

    boolean existsByGroup_Leader_IdAndMemberRoleNot(Long leaderId, MemberRole role);

    Optional<Membership> findByUser_IdAndGroup_Id(Long userId, Long groupId);

    void deleteByUser_IdAndGroup_Id(Long userId, Long groupId);

    List<Membership> findByGroup_IdAndJoinStatus(Long groupId, JoinStatus joinStatus);
}
