package team.budderz.buddyspace.domain.group.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;
import team.budderz.buddyspace.domain.membership.exception.MembershipErrorCode;
import team.budderz.buddyspace.domain.membership.exception.MembershipException;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupPermission;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.group.repository.GroupPermissionRepository;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class GroupValidator {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final GroupPermissionRepository groupPermissionRepository;

    /**
     * 모임 존재 여부 확인 및 반환
     *
     * @param groupId 모임 ID
     * @return 조회된 모임 정보
     */
    public Group findGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));
    }

    /**
     * 사용자 모임 리더 여부 검증
     *
     * @param userId  사용자 ID
     * @param groupId 모임 ID
     */
    public void validateLeader(Long userId, Long groupId) {
        existsUser(userId);
        Group group = findGroupOrThrow(groupId);
        Membership membership = findMembershipOrThrow(userId, groupId);

        if (!group.getLeader().getId().equals(userId) || membership.getMemberRole() != MemberRole.LEADER) {
            throw new GroupException(GroupErrorCode.FUNCTION_ACCESS_DENIED);
        }
    }

    /**
     * 사용자 모임 멤버 여부 검증
     *
     * @param userId  사용자 ID
     * @param groupId 모임 ID
     */
    public void validateMember(Long userId, Long groupId) {
        existsUser(userId);
        existsGroup(groupId);
        Membership membership = findMembershipOrThrow(userId, groupId);

        if (membership.getJoinStatus() != JoinStatus.APPROVED) {
            throw new MembershipException(MembershipErrorCode.NOT_APPROVED_MEMBER);
        }
    }

    /**
     * 콘텐츠 소유자 여부 검증
     *
     * @param loginUserId 로그인 사용자 ID
     * @param groupId     모임 ID
     * @param authorId    콘텐츠 생성자 ID
     */
    public void validateOwner(Long loginUserId, Long groupId, Long authorId) {
        validateMember(loginUserId, groupId);

        if (!loginUserId.equals(authorId)) {
            throw new GroupException(GroupErrorCode.FUNCTION_ACCESS_DENIED);
        }
    }

    /**
     * 사용자의 모임 기능별 접근 권한 검증 (생성)
     *
     * @param userId  사용자 ID
     * @param groupId 모임 ID
     * @param type    실행할 기능 (CREATE_*)
     */
    public void validatePermission(Long userId, Long groupId, PermissionType type) {
        validateMember(userId, groupId);
        validateCreatePermission(userId, groupId, type);
    }

    /**
     * 사용자의 모임 기능별 접근 권한 검증 (삭제)
     *
     * @param userId   사용자 ID
     * @param groupId  모임 ID
     * @param type     실행할 기능 (DELETE_*)
     * @param authorId 삭제할 콘텐츠의 생성자 ID
     */
    public void validatePermission(Long userId, Long groupId, PermissionType type, Long authorId) {
        validateMember(userId, groupId);
        switch (type.getAction()) {
            case CREATE -> validateCreatePermission(userId, groupId, type);
            case DELETE -> validateDeletePermission(userId, groupId, type, authorId);
            default -> throw new GroupException(GroupErrorCode.PERMISSION_TYPE_NOT_SUPPORTED);
        }
    }

    public void validateUserCanBeDeleted(Long userId) {
        boolean hasOtherMembers
                = membershipRepository.existsByGroup_Leader_IdAndMemberRoleNot(userId, MemberRole.LEADER);
        if (hasOtherMembers) {
            throw new GroupException(GroupErrorCode.MEMBERS_EXIST_IN_GROUP);
        }
    }

    public boolean isExistsGroupByCode(String code) {
        return groupRepository.existsByInviteCode(code);
    }

    public Group findGroupByCode(String code) {
        return groupRepository.findByInviteCode(code)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));
    }

    private void existsGroup(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new GroupException(GroupErrorCode.GROUP_NOT_FOUND);
        }
    }

    private void existsUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    private Membership findMembershipOrThrow(Long userId, Long groupId) {
        return membershipRepository.findByUser_IdAndGroup_Id(userId, groupId)
                .orElseThrow(() -> new MembershipException(MembershipErrorCode.MEMBERSHIP_NOT_FOUND));
    }

    private void validateCreatePermission(Long userId, Long groupId, PermissionType type) {
        MemberRole memberRole = getMemberRole(userId, groupId);
        MemberRole allowedRole = getAllowedRole(groupId, type);

        if (memberRole.getPriority() < allowedRole.getPriority()) {
            throw new GroupException(GroupErrorCode.FUNCTION_ACCESS_DENIED);
        }
    }

    private void validateDeletePermission(Long userId, Long groupId, PermissionType type, Long authorId) {
        if (isOwner(userId, authorId)) return;

        MemberRole memberRole = getMemberRole(userId, groupId);
        MemberRole allowedRole = getAllowedRole(groupId, type);

        if (memberRole == MemberRole.LEADER) return;

        if (memberRole.getPriority() < allowedRole.getPriority()) {
            throw new GroupException(GroupErrorCode.FUNCTION_ACCESS_DENIED);
        }
    }

    private MemberRole getMemberRole(Long userId, Long groupId) {
        Membership membership = findMembershipOrThrow(userId, groupId);
        return membership.getMemberRole();
    }

    private MemberRole getAllowedRole(Long groupId, PermissionType type) {
        return groupPermissionRepository.findByGroup_IdAndType(groupId, type)
                .map(GroupPermission::getRole)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_PERMISSION_NOT_FOUND));
    }

    private boolean isOwner(Long userId, Long authorId) {
        return userId.equals(authorId);
    }
}
