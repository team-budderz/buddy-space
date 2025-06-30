package team.budderz.buddyspace.domain.membership.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.membership.response.MemberResponse;
import team.budderz.buddyspace.api.membership.response.MembershipResponse;
import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.domain.membership.exception.MembershipErrorCode;
import team.budderz.buddyspace.domain.membership.exception.MembershipException;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.domain.user.provider.UserProfileImageProvider;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;
import team.budderz.buddyspace.infra.database.membership.entity.JoinPath;
import team.budderz.buddyspace.infra.database.membership.entity.JoinStatus;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final GroupValidator validator;
    private final UserProfileImageProvider profileImageProvider;

    /**
     * 로그인한 사용자가 가입되어 있는 특정 모임과의 멤버십 정보 조회
     *
     * @param userId  로그인 사용자 ID
     * @param groupId 모임 ID
     * @return 멤버십 정보
     */
    @Transactional(readOnly = true)
    public MemberResponse findMyMembership(Long userId, Long groupId) {
        validator.validateMember(userId, groupId);
        Membership membership = findMembershipByUserAndGroup(userId, groupId);

        String profileImageUrl = profileImageProvider.getProfileImageUrl(membership.getUser());

        return MemberResponse.of(
                membership.getUser().getId(),
                membership.getUser().getName(),
                profileImageUrl,
                membership.getMemberRole(),
                membership.getJoinStatus(),
                membership.getJoinPath(),
                membership.getJoinedAt()
        );
    }

    /**
     * 모임 가입 요청
     *
     * @param userId  사용자 ID
     * @param groupId 모임 ID
     * @return 가입 요청 정보
     */
    @Transactional
    public MembershipResponse requestJoin(Long userId, Long groupId) {
        User user = findUserById(userId);
        Group group = validator.findGroupOrThrow(groupId);

        // 이미 가입 요청/승인 중이거나 차단된 회원일 경우 예외
        membershipRepository.findByUser_IdAndGroup_Id(userId, groupId).ifPresent(membership -> {
            if (membership.getJoinStatus() == JoinStatus.REQUESTED) {
                throw new MembershipException(MembershipErrorCode.ALREADY_REQUESTED);
            }
            if (membership.getJoinStatus() == JoinStatus.APPROVED) {
                throw new MembershipException(MembershipErrorCode.ALREADY_JOINED);
            }
            if (membership.getJoinStatus() == JoinStatus.BLOCKED) {
                throw new MembershipException(MembershipErrorCode.BLOCKED_MEMBER);
            }
        });

        // 오프라인, 온/오프라인 모임의 경우 검증
        if (!group.getType().equals(GroupType.ONLINE) && group.getIsNeighborhoodAuthRequired() != null) {
            // 동네 인증 사용자만 가입 요청 가능한 모임에 동네 미인증 사용자인 경우 예외
            if (group.getIsNeighborhoodAuthRequired() && user.getNeighborhood() == null) {
                throw new GroupException(GroupErrorCode.NEIGHBOR_VERIFICATION_REQUIRED);
            }
            // 모임의 동네와 사용자의 동네가 일치하지 않을 경우 예외
            if (!group.getAddress().equals(user.getAddress())) {
                throw new GroupException(GroupErrorCode.NOT_A_NEIGHBOR_GROUP);
            }
        }

        Membership membership = Membership.fromRequest(user, group);
        membershipRepository.save(membership);

        return MembershipResponse.of(group, List.of(membership), profileImageProvider);
    }

    /**
     * 가입 요청 취소
     *
     * @param loginUserId 로그인 사용자 ID
     * @param groupId     모임 ID
     */
    @Transactional
    public void cancelRequest(Long loginUserId, Long groupId) {
        Membership membership = findMembershipByUserAndGroup(loginUserId, groupId);

        if (membership.getJoinStatus() != JoinStatus.REQUESTED) {
            throw new MembershipException(MembershipErrorCode.NOT_REQUESTED_MEMBER);
        }

        membershipRepository.deleteByUser_IdAndGroup_Id(loginUserId, groupId);
    }

    /**
     * 가입 요청 승인
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @param memberId    가입 요청한 멤버 ID
     * @return 해당 멤버의 가입 상태 정보
     */
    @Transactional
    public MembershipResponse approveMember(Long loginUserId, Long groupId, Long memberId) {
        Group group = validator.findGroupOrThrow(groupId);
        validator.validateLeader(loginUserId, groupId);

        Membership membership = findMembershipByUserAndGroup(memberId, groupId);

        if (membership.getJoinStatus() != JoinStatus.REQUESTED) {
            throw new MembershipException(MembershipErrorCode.NOT_REQUESTED_MEMBER);
        }

        membership.approve();

        return MembershipResponse.of(group, List.of(membership), profileImageProvider);
    }

    /**
     * 가입 요청 거절
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @param memberId    가입 요청한 멤버 ID
     */
    @Transactional
    public void rejectMember(Long loginUserId, Long groupId, Long memberId) {
        validator.validateLeader(loginUserId, groupId);

        Membership membership = findMembershipByUserAndGroup(memberId, groupId);

        if (membership.getJoinStatus() != JoinStatus.REQUESTED) {
            throw new MembershipException(MembershipErrorCode.NOT_REQUESTED_MEMBER);
        }

        membershipRepository.deleteByUser_IdAndGroup_Id(memberId, groupId);
    }

    /**
     * 초대 링크로 모임 가입
     *
     * @param loginUserId 로그인 사용자 ID
     * @param inviteCode  초대 코드
     * @return 해당 멤버의 가입 상태 정보
     */
    @Transactional
    public MembershipResponse inviteJoin(Long loginUserId, String inviteCode) {
        Group group = validator.findGroupByCode(inviteCode);
        User user = findUserById(loginUserId);

        Optional<Membership> existing = membershipRepository.findByUser_IdAndGroup_Id(user.getId(), group.getId());

        Membership membership = existing.map(m -> {
            if (m.getJoinStatus() == JoinStatus.APPROVED) {
                throw new MembershipException(MembershipErrorCode.ALREADY_JOINED);
            }
            if (m.getJoinStatus() == JoinStatus.BLOCKED) {
                throw new MembershipException(MembershipErrorCode.BLOCKED_MEMBER);
            }

            // 가입 요청 중인 경우 승인 처리
            m.approve();
            m.updateJoinPath(JoinPath.INVITE);
            return m;

        }).orElseGet(() -> {
            Membership newMembership = Membership.fromInvite(user, group);
            return membershipRepository.save(newMembership);
        });

        return MembershipResponse.of(group, List.of(membership), profileImageProvider);
    }

    /**
     * 모임 멤버 강제 탈퇴
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @param memberId    강제 탈퇴할 멤버 ID
     */
    @Transactional
    public void expelMember(Long loginUserId, Long groupId, Long memberId) {
        validator.validateLeader(loginUserId, groupId);
        validator.validateMember(memberId, groupId);

        membershipRepository.deleteByUser_IdAndGroup_Id(memberId, groupId);
    }

    /**
     * 모임 멤버 차단
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @param memberId    차단할 멤버 ID
     * @return 해당 멤버의 가입 상태 정보
     */
    @Transactional
    public MembershipResponse blockMember(Long loginUserId, Long groupId, Long memberId) {
        Group group = validator.findGroupOrThrow(groupId);
        validator.validateLeader(loginUserId, groupId);
        validator.validateMember(memberId, groupId);

        Membership membership = findMembershipByUserAndGroup(memberId, groupId);
        membership.block();

        return MembershipResponse.of(group, List.of(membership), profileImageProvider);
    }

    /**
     * 모임 멤버 차단 해제
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @param memberId    차단 해제할 멤버 ID
     */
    @Transactional
    public void unblockMember(Long loginUserId, Long groupId, Long memberId) {
        validator.validateLeader(loginUserId, groupId);

        Membership membership = findMembershipByUserAndGroup(memberId, groupId);

        if (membership.getJoinStatus() != JoinStatus.BLOCKED) {
            throw new MembershipException(MembershipErrorCode.NOT_BLOCKED_MEMBER);
        }

        membershipRepository.deleteByUser_IdAndGroup_Id(memberId, groupId);
    }

    /**
     * 모임 멤버 권한 설정
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @param memberId    권한을 변경할 모임 멤버 ID
     * @param role        변경할 권한 정보
     * @return 변경된 모임 멤버의 권한 정보
     */
    @Transactional
    public MembershipResponse updateMemberRole(Long loginUserId, Long groupId, Long memberId, MemberRole role) {
        if (role == MemberRole.LEADER) {
            throw new MembershipException(MembershipErrorCode.LEADER_ROLE_UNIQUE);
        }

        Group group = validator.findGroupOrThrow(groupId);
        validator.validateLeader(loginUserId, groupId);

        if (loginUserId.equals(memberId)) {
            throw new MembershipException(MembershipErrorCode.CANNOT_CHANGE_OWN_ROLE);
        }

        validator.validateMember(memberId, groupId);
        Membership membership = findMembershipByUserAndGroup(memberId, groupId);

        membership.updateMemberRole(role);

        return MembershipResponse.of(group, List.of(membership), profileImageProvider);
    }

    /**
     * 모임 리더 위임
     *
     * @param loginUserId 로그인 사용자 ID (전 리더)
     * @param groupId     모임 ID
     * @param memberId    위임할 멤버 ID (새 리더)
     * @return 변경된 모임 멤버 권한 정보 리스트
     */
    @Transactional
    public MembershipResponse delegateLeader(Long loginUserId, Long groupId, Long memberId) {
        Group group = validator.findGroupOrThrow(groupId);
        validator.validateLeader(loginUserId, groupId);
        validator.validateMember(memberId, groupId);

        Membership oldLeader = findMembershipByUserAndGroup(loginUserId, groupId);
        Membership newLeader = findMembershipByUserAndGroup(memberId, groupId);

        group.updateLeader(newLeader.getUser());
        oldLeader.updateMemberRole(MemberRole.MEMBER);
        newLeader.updateMemberRole(MemberRole.LEADER);

        return MembershipResponse.of(group, List.of(oldLeader, newLeader), profileImageProvider);
    }

    /**
     * 모임에 가입된 멤버 목록 조회
     *
     * @param loginUserId 로그인 사용자 ID
     * @param groupId     모임 ID
     * @return 특정 모임에 가입된 멤버 목록 정보
     */
    @Transactional(readOnly = true)
    public MembershipResponse findApprovedMembers(Long loginUserId, Long groupId) {
        Group group = validator.findGroupOrThrow(groupId);
        validator.validateMember(loginUserId, groupId);

        List<Membership> members = membershipRepository.findByGroup_IdAndJoinStatus(groupId, JoinStatus.APPROVED);

        return MembershipResponse.of(group, members, profileImageProvider);
    }

    /**
     * 모임에 가입 요청 중인 회원 목록 조회
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @return 특정 모임에 가입 요청 중인 회원 목록 정보
     */
    @Transactional(readOnly = true)
    public MembershipResponse findRequestedMembers(Long loginUserId, Long groupId) {
        Group group = validator.findGroupOrThrow(groupId);
        validator.validateLeader(loginUserId, groupId);

        List<Membership> members = membershipRepository.findByGroup_IdAndJoinStatus(groupId, JoinStatus.REQUESTED);

        return MembershipResponse.of(group, members, profileImageProvider);
    }

    /**
     * 모임에서 차단된 회원 목록 조회
     *
     * @param loginUserId 로그인 사용자 ID (모임 리더)
     * @param groupId     모임 ID
     * @return 특정 모임에서 차단된 회원 목록 정보
     */
    @Transactional(readOnly = true)
    public MembershipResponse findBlockedMembers(Long loginUserId, Long groupId) {
        Group group = validator.findGroupOrThrow(groupId);
        validator.validateLeader(loginUserId, groupId);

        List<Membership> members = membershipRepository.findByGroup_IdAndJoinStatus(groupId, JoinStatus.BLOCKED);

        return MembershipResponse.of(group, members, profileImageProvider);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private Membership findMembershipByUserAndGroup(Long userId, Long groupId) {
        return membershipRepository.findByUser_IdAndGroup_Id(userId, groupId)
                .orElseThrow(() -> new MembershipException(MembershipErrorCode.MEMBERSHIP_NOT_FOUND));
    }
}
