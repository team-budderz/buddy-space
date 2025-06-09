package team.budderz.buddyspace.infra.database.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;

import java.util.List;
import java.util.Optional;

public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long> {

    void deleteAllByGroup_Id(Long groupId);

    Optional<GroupPermission> findByGroup_IdAndType(Long groupId, PermissionType type);

    List<GroupPermission> findByGroup_Id(Long groupId);
}