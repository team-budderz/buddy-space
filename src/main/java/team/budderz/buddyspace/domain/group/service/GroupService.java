package team.budderz.buddyspace.domain.group.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.group.response.GroupResponse;
import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupAccess;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;
import team.budderz.buddyspace.infra.database.group.repository.GroupPermissionRepository;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.membership.entity.MembershipRole;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.mission.repository.MissionRepository;
import team.budderz.buddyspace.infra.database.post.repository.PostRepository;
import team.budderz.buddyspace.infra.database.schedule.repository.ScheduleRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;
import team.budderz.buddyspace.infra.database.vote.repository.VoteRepository;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;

    private final PostRepository postRepository;
    private final MissionRepository missionRepository;
    private final VoteRepository voteRepository;
    private final ScheduleRepository scheduleRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final GroupPermissionRepository groupPermissionRepository;

    private final GroupPermissionService groupPermissionService;

    /**
     * 모임 생성
     *
     * @param userId        로그인 사용자 ID
     * @param name          모임 이름
     * @param coverImageUrl 모임 커버 이미지 주소
     * @param access        모임 공개 범위
     * @param type          모임 유형
     * @param interest      모임 관심사
     * @return 생성된 모임 정보(이름, 공개 범위, 유형, 관심사)
     */
    @Transactional
    public GroupResponse saveGroup(Long userId,
                                   String name,
                                   String coverImageUrl,
                                   GroupAccess access,
                                   GroupType type,
                                   GroupInterest interest) {

        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (StringUtils.isBlank(coverImageUrl)) {
            coverImageUrl = type.getDefaultCoverImageUrl();
        }

        Group group = new Group(name, coverImageUrl, access, type, interest, leader);
        Group saved = groupRepository.save(group);

        Membership membership = Membership.fromCreator(leader, group);
        membershipRepository.save(membership);

        groupPermissionService.saveDefaultPermission(group);

        return GroupResponse.from(saved);
    }

    /**
     * 모임 정보 수정
     *
     * @param userId 로그인 사용자 ID
     * @param groupId 모임 ID
     * @param name 변경할 모임 이름
     * @param coverImageUrl 변경할 커버 이미지 주소
     * @param access 변경할 모임 공개 범위
     * @param type 변경할 모임 유형
     * @param interest 변경할 모임 관심사
     * @return 변경된 모임 정보
     */
    @Transactional
    public GroupResponse updateGroup(Long userId,
                                     Long groupId,
                                     String name,
                                     String coverImageUrl,
                                     GroupAccess access,
                                     GroupType type,
                                     GroupInterest interest) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

        if (!group.getLeader().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.FUNCTION_ACCESS_DENIED);
        }

        if (StringUtils.isBlank(coverImageUrl)) {
            coverImageUrl = type.getDefaultCoverImageUrl();
        }

        group.updateGroupInfo(name, coverImageUrl, access, type, interest);

        return GroupResponse.from(group);
    }

    /**
     * 모임 삭제
     *
     * @param userId 로그인 사용자 ID
     * @param groupId 모임 ID
     */
    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

        if (!group.getLeader().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.FUNCTION_ACCESS_DENIED);
        }

        boolean hasOtherMembers =
                membershipRepository.existsByGroup_IdAndMembershipRoleNot(groupId, MembershipRole.LEADER);

        if (hasOtherMembers) {
            throw new GroupException(GroupErrorCode.MEMBERS_EXIST_IN_GROUP);
        }

        deleteAllGroupRelatedData(groupId);
        groupRepository.delete(group);
    }

    /**
     * 모임 관련 데이터 전체 삭제
     *
     * @param groupId 모임 ID
     */
    private void deleteAllGroupRelatedData(Long groupId) {
//        postRepository.deleteAllByGroup_Id(groupId);
//        missionRepository.deleteAllByGroup_Id(groupId);
//        voteRepository.deleteAllByGroup_Id(groupId);
//        scheduleRepository.deleteAllByGroup_Id(groupId);
//        chatRoomRepository.deleteAllByGroup_Id(groupId);
        membershipRepository.deleteAllByGroup_Id(groupId);
        groupPermissionRepository.deleteAllByGroup_Id(groupId);
    }
}
