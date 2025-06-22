package team.budderz.buddyspace.domain.group.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.attachment.response.AttachmentResponse;
import team.budderz.buddyspace.api.group.request.SaveGroupRequest;
import team.budderz.buddyspace.api.group.request.UpdateGroupRequest;
import team.budderz.buddyspace.api.group.response.GroupListResponse;
import team.budderz.buddyspace.api.group.response.GroupResponse;
import team.budderz.buddyspace.domain.attachment.service.AttachmentService;
import team.budderz.buddyspace.domain.group.exception.GroupErrorCode;
import team.budderz.buddyspace.domain.group.exception.GroupException;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.domain.user.exception.UserErrorCode;
import team.budderz.buddyspace.domain.user.exception.UserException;
import team.budderz.buddyspace.global.response.PageResponse;
import team.budderz.buddyspace.infra.client.s3.S3Directory;
import team.budderz.buddyspace.infra.client.s3.DefaultImageProvider;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.GroupInterest;
import team.budderz.buddyspace.infra.database.group.entity.GroupSortType;
import team.budderz.buddyspace.infra.database.group.entity.GroupType;
import team.budderz.buddyspace.infra.database.group.repository.GroupPermissionRepository;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.membership.entity.MemberRole;
import team.budderz.buddyspace.infra.database.membership.entity.Membership;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.mission.repository.MissionRepository;
import team.budderz.buddyspace.infra.database.post.repository.PostRepository;
import team.budderz.buddyspace.infra.database.schedule.repository.ScheduleRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;
import team.budderz.buddyspace.infra.database.vote.repository.VoteRepository;

import java.util.List;

import static team.budderz.buddyspace.domain.group.constant.GroupDefaults.DEFAULT_PAGE_SIZE;

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
    private final GroupValidator validator;
    private final AttachmentService attachmentService;
    private final DefaultImageProvider defaultImageProvider;

    /**
     * 모임 생성
     * - 커버 이미지, 리더, 권한 정보 설정
     *
     * @param userId     로그인한 사용자 ID
     * @param request    모임 생성 요청 DTO
     * @param coverImage 업로드할 커버 이미지 (nullable)
     * @return 생성된 모임 정보
     */
    @Transactional
    public GroupResponse saveGroup(Long userId, SaveGroupRequest request, MultipartFile coverImage) {

        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Group group = new Group(request, leader);

        // 오프라인 모임일 경우 모임 동네 정보를 리더의 동네로 저장
        if (!group.getType().equals(GroupType.ONLINE)) {
            group.updateAddress(leader.getAddress());
        }

        // 커버 이미지 업로드 및 Attachment 조회
        Attachment coverAttachment = getCoverAttachment(coverImage, userId);
        // 모임 커버 이미지 설정
        group.updateCoverAttachment(coverAttachment);

        Group saved = groupRepository.save(group);

        // 모임 생성자를 리더 권한의 회원으로 저장
        Membership membership = Membership.fromCreator(leader, group);
        membershipRepository.save(membership);

        // 모임 기능별 권한 기본값 설정
        groupPermissionService.saveDefaultPermission(group);

        // 커버 이미지 url 생성
        String coverImageUrl = getCoverImageUrl(group, coverAttachment);

        return GroupResponse.from(saved, coverImageUrl);
    }

    /**
     * 모임 정보 수정 및 커버 이미지 변경
     *
     * @param userId     로그인 사용자 ID
     * @param groupId    모임 ID
     * @param request    모임 수정 요청 DTO
     * @param coverImage 새 커버 이미지 (nullable)
     * @return 수정된 모임 정보
     */
    @Transactional
    public GroupResponse updateGroup(Long userId, Long groupId, UpdateGroupRequest request, MultipartFile coverImage) {
        // 리더 여부 검증
        validator.validateLeader(userId, groupId);
        Group group = validator.findGroupOrThrow(groupId);

        // 모임 정보 업데이트
        group.updateGroupInfo(request);

        // 기존 커버 이미지가 기본 이미지가 아니면 삭제
        Attachment oldAttachment = group.getCoverAttachment();
        if (oldAttachment != null && !defaultImageProvider.isDefaultGroupCoverKey(oldAttachment.getKey())) {
            attachmentService.delete(oldAttachment.getId());
        }

        // 새 커버 이미지 업로드 및 설정
        Attachment coverAttachment = getCoverAttachment(coverImage, userId);
        group.updateCoverAttachment(coverAttachment);

        // 커버 이미지 url
        String coverImageUrl = getCoverImageUrl(group, coverAttachment);

        return GroupResponse.from(group, coverImageUrl);
    }

    /**
     * 사용자가 가입한 모임 목록 조회
     *
     * @param userId 사용자 ID
     * @param page   페이지 번호
     * @return 조회된 모임 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<GroupListResponse> findGroupsByUser(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        Page<GroupListResponse> result = groupRepository.findGroupsByUser(userId, pageable);

        return PageResponse.from(generateCoverImageUrls(result));
    }

    /**
     * 온라인 모임 목록 조회
     * - 관심사 및 정렬 기준 기반
     *
     * @param sortType 정렬 기준
     * @param interest 관심사 (nullable)
     * @param page     페이지 번호
     * @return 조회된 모임 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<GroupListResponse> findOnlineGroups(GroupSortType sortType, String interest, int page) {
        GroupInterest interestType = StringUtils.isNotBlank(interest) ? GroupInterest.fromName(interest) : null;
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        Page<GroupListResponse> result = groupRepository.findOnlineGroups(sortType, interestType, pageable);

        return PageResponse.from(generateCoverImageUrls(result));
    }

    /**
     * 오프라인 모임 목록 조회
     * - 로그인한 사용자의 동네 기반
     *
     * @param userId   사용자 ID
     * @param sortType 정렬 기준
     * @param interest 관심사 (nullable)
     * @param page     페이지 번호
     * @return 조회된 모임 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<GroupListResponse> findOfflineGroups(Long userId, GroupSortType sortType, String interest, int page) {
        GroupInterest interestType = StringUtils.isNotBlank(interest) ? GroupInterest.fromName(interest) : null;
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Page<GroupListResponse> result = groupRepository.findOfflineGroups(user.getAddress(), sortType, interestType, pageable);
        return PageResponse.from(generateCoverImageUrls(result));
    }

    /**
     * 모임 이름 검색
     *
     * @param keyword  검색 키워드
     * @param interest 관심사
     * @param page     페이지 번호
     * @return 검색 결과 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<GroupListResponse> searchGroupsByName(String keyword, String interest, int page) {
        GroupInterest interestType = StringUtils.isNotBlank(interest) ? GroupInterest.fromName(interest) : null;
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);

        if (StringUtils.isBlank(keyword)) {
            return PageResponse.from(Page.empty(pageable)); // 검색 키워드 없이 요청한 경우 빈 페이지 반환
        }

        Page<GroupListResponse> result = groupRepository.searchGroupsByName(keyword, interestType, pageable);
        return PageResponse.from(generateCoverImageUrls(result));
    }

    /**
     * 모임 삭제
     * - 리더 외의 회원이 존재하면 삭제 불가
     * - 모임 관련 모든 데이터 함께 삭제
     *
     * @param userId  로그인 사용자 ID
     * @param groupId 모임 ID
     */
    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        validator.validateLeader(userId, groupId);
        Group group = validator.findGroupOrThrow(groupId);

        // 모임에 리더를 제외하고 가입된 회원이 존재하는지 확인
        boolean hasOtherMembers =
                membershipRepository.existsByGroup_IdAndMemberRoleNot(groupId, MemberRole.LEADER);

        // 리더를 제외하고 가입된 회원이 있으면 모임 삭제 불가
        if (hasOtherMembers) {
            throw new GroupException(GroupErrorCode.MEMBERS_EXIST_IN_GROUP);
        }

        Attachment coverAttachment = group.getCoverAttachment();
        // 모임 커버 이미지가 기본 이미지가 아니면 삭제
        if (coverAttachment != null && !defaultImageProvider.isDefaultGroupCoverKey(coverAttachment.getKey())) {
            attachmentService.delete(coverAttachment.getId());
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
        postRepository.deleteAllByGroup_Id(groupId);
        missionRepository.deleteAllByGroup_Id(groupId);
        voteRepository.deleteAllByGroup_Id(groupId);
        scheduleRepository.deleteAllByGroup_Id(groupId);
        chatRoomRepository.deleteAllByGroup_Id(groupId);
        membershipRepository.deleteAllByGroup_Id(groupId);
        groupPermissionRepository.deleteAllByGroup_Id(groupId);
    }

    // 커버 이미지 업로드 및 Attachment 조회
    private Attachment getCoverAttachment(MultipartFile coverImage, Long userId) {
        if (coverImage == null || coverImage.isEmpty()) {
            return null; // 기본 이미지인 경우 null
        }
        AttachmentResponse uploaded = attachmentService.upload(coverImage, userId, S3Directory.GROUP_COVER);
        return attachmentService.findAttachmentOrThrow(uploaded.id());
    }

    // 커버 이미지 url 반환
    private String getCoverImageUrl(Group group, Attachment coverAttachment) {
        if (coverAttachment == null) {
            // null 일 경우 모임 유형 기반 기본 이미지 반환
            return defaultImageProvider.getDefaultGroupCoverImageUrl(group.getType());
        }
        return attachmentService.getViewUrl(coverAttachment.getId());
    }

    // 모임 목록 조회 응답에 커버 이미지 조회용 url 삽입
    private Page<GroupListResponse> generateCoverImageUrls(Page<GroupListResponse> result) {
        List<GroupListResponse> contents = result.getContent().stream()
                .map(group -> {
                    String url;

                    if (group.coverAttachmentId() != null) {
                        url = attachmentService.getViewUrl(group.coverAttachmentId());
                    } else {
                        url = defaultImageProvider.getDefaultGroupCoverImageUrl(group.groupType());
                    }

                    return group.withCoverImageUrl(url);
                })
                .toList();

        return new PageImpl<>(contents, result.getPageable(), result.getTotalElements());
    }
}
