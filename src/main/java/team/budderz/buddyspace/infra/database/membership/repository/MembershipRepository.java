package team.budderz.buddyspace.infra.database.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.membership.entity.MembershipRole;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    @Modifying(clearAutomatically = true)
    void deleteAllByUser_Id(Long userId);

    void deleteAllByGroup_Id(Long groupId);

    boolean existsByGroup_IdAndMembershipRoleNot(Long groupId, MembershipRole role);

    // 그룹 참여 여부
    boolean existsByUser_IdAndGroup_Id(Long userId, Long groupId);
}
