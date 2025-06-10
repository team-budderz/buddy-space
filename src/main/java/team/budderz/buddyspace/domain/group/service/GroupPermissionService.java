package team.budderz.buddyspace.domain.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.group.request.GroupPermissionRequest;
import team.budderz.buddyspace.api.group.response.GroupPermissionResponse;
import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;
import team.budderz.buddyspace.infra.database.group.entity.PermissionAction;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.group.repository.GroupPermissionRepository;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupPermissionService {

    private final GroupPermissionRepository groupPermissionRepository;
    private final GroupValidator validator;

    /**
     * 모임 기능별 권한 설정
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @param requests    설정할 기능별 권한 정보
     * @return 설정된 기능별 권한 정보
     */
    @Transactional
    public GroupPermissionResponse updateGroupPermission(Long loginUserId,
                                                         Long groupId,
                                                         List<GroupPermissionRequest> requests) {
        // 요청에서 전달받은 권한 설정 리스트에서 모든 PermissionType 을 Set 으로 변환
        Set<PermissionType> requestedTypes = requests.stream()
                .map(GroupPermissionRequest::type)
                .collect(Collectors.toSet());

        // 시스템에 정의된 모든 PermissionType 값을 Set 으로 생성 (필수 권한 목록)
        Set<PermissionType> requiredTypes = Arrays.stream(PermissionType.values())
                .collect(Collectors.toSet());

        // 요청에 포함된 권한 타입이 모든 필수 타입을 포함하지 않으면 예외 발생
        if (!requestedTypes.containsAll(requiredTypes)) {
            throw new GroupException(GroupErrorCode.MISSING_PERMISSION_TYPE);
        }

        validator.validateLeader(loginUserId, groupId);
        Group group = validator.findGroupOrThrow(groupId);

        // 기존 모임 권한 설정 전체 삭제 (초기화)
        groupPermissionRepository.deleteAllByGroup_Id(groupId);

        // 요청으로 들어온 권한 설정 저장
        List<GroupPermission> saved = requests.stream()
                .map(request -> {
                    PermissionType type = request.type();
                    MemberRole role = request.role();

                    // 삭제 권한은 리더/부리더만 가능
                    if (type.getAction() == PermissionAction.DELETE && role == MemberRole.MEMBER) {
                        throw new GroupException(GroupErrorCode.INVALID_PERMISSION_SETTING);
                    }

                    GroupPermission permission = GroupPermission.of(group, role, type);
                    return groupPermissionRepository.save(permission);
                })
                .toList();

        return GroupPermissionResponse.of(group, saved);
    }

    /**
     * 모임 기능별 접근 권한 조회
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId 모임 ID
     * @return 조회된 기능별 권한 정보
     */
    @Transactional(readOnly = true)
    public GroupPermissionResponse findGroupPermissions(Long loginUserId, Long groupId) {
        Group group = validator.findGroupOrThrow(groupId);
        validator.validateLeader(loginUserId, groupId);

        List<GroupPermission> permissions = groupPermissionRepository.findByGroup_Id(groupId);

        return GroupPermissionResponse.of(group, permissions);
    }

    /**
     * 모임 기능별 기본 권한 설정
     * - 모임 생성 시 사용
     *
     * @param group 모임 ID
     */
    @Transactional
    public void saveDefaultPermission(Group group) {
        List<GroupPermission> defaultPermissions = List.of(
                GroupPermission.of(group, MemberRole.MEMBER, PermissionType.CREATE_POST),
                GroupPermission.of(group, MemberRole.MEMBER, PermissionType.CREATE_SCHEDULE),
                GroupPermission.of(group, MemberRole.MEMBER, PermissionType.CREATE_VOTE),
                GroupPermission.of(group, MemberRole.LEADER, PermissionType.DELETE_POST),
                GroupPermission.of(group, MemberRole.LEADER, PermissionType.DELETE_SCHEDULE),
                GroupPermission.of(group, MemberRole.LEADER, PermissionType.CREATE_MISSION),
                GroupPermission.of(group, MemberRole.LEADER, PermissionType.DELETE_MISSION),
                GroupPermission.of(group, MemberRole.LEADER, PermissionType.DELETE_VOTE),
                GroupPermission.of(group, MemberRole.LEADER, PermissionType.CREATE_DIRECT_CHAT_ROOM),
                GroupPermission.of(group, MemberRole.LEADER, PermissionType.CREATE_INVITE_LINK)
        );

        groupPermissionRepository.saveAll(defaultPermissions);
    }
}
