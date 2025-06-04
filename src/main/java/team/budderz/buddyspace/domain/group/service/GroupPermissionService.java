package team.budderz.buddyspace.domain.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.group.repository.GroupPermissionRepository;
import team.budderz.buddyspace.infra.database.membership.entity.MembershipRole;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupPermissionService {

    private final GroupPermissionRepository groupPermissionRepository;

    public void saveDefaultPermission(Group group) {

        List<GroupPermission> defaultPermissions = List.of(
                GroupPermission.of(group, MembershipRole.MEMBER, PermissionType.CREATE_POST),
                GroupPermission.of(group, MembershipRole.MEMBER, PermissionType.CREATE_SCHEDULE),
                GroupPermission.of(group, MembershipRole.LEADER, PermissionType.DELETE_POST),
                GroupPermission.of(group, MembershipRole.LEADER, PermissionType.DELETE_SCHEDULE),
                GroupPermission.of(group, MembershipRole.LEADER, PermissionType.CREATE_MISSION),
                GroupPermission.of(group, MembershipRole.LEADER, PermissionType.DELETE_MISSION),
                GroupPermission.of(group, MembershipRole.LEADER, PermissionType.CREATE_VOTE),
                GroupPermission.of(group, MembershipRole.LEADER, PermissionType.DELETE_VOTE),
                GroupPermission.of(group, MembershipRole.LEADER, PermissionType.CREATE_CHAT_ROOM)
        );

        groupPermissionRepository.saveAll(defaultPermissions);
    }
}
