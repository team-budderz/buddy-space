package team.budderz.buddyspace.infra.database.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;

public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long> {

    void deleteAllByGroup_Id(Long groupId);
}
