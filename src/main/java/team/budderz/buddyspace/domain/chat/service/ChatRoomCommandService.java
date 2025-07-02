package team.budderz.buddyspace.domain.chat.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import team.budderz.buddyspace.api.chat.request.rest.AddParticipantRequest;
import team.budderz.buddyspace.api.chat.request.rest.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.request.rest.UpdateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.rest.CreateChatRoomResponse;
import team.budderz.buddyspace.api.chat.response.rest.UpdateChatRoomResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.domain.chat.validator.ChatValidator;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoomType;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 채팅방 생성, 수정, 삭제, 참여자 관리 등의 쓰기 작업을 담당하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomCommandService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final GroupValidator groupValidator;
    private final ChatValidator chatValidator;
    private final ChatMemberEventService chatMemberEventService;

    /**
     * 채팅방을 생성합니다.
     *
     * @param groupId 그룹 ID
     * @param userId 사용자 ID
     * @param request 채팅방 생성 요청
     * @return 생성된 채팅방 정보
     */
    @Operation(summary = "채팅방 생성", description = "그룹에 참여한 사용자가 채팅방을 생성합니다. DIRECT 타입은 중복 불가.")
    @ApiResponse(responseCode = "200", description = "채팅방 생성 성공")
    public CreateChatRoomResponse createChatRoom(Long groupId, Long userId, CreateChatRoomRequest request) {
        // 그룹, 유저, 멤버 검증
        Group group = groupValidator.findGroupOrThrow(groupId);
        User createdBy = findUserOrThrow(userId);
        groupValidator.validateMember(userId, groupId);

        // 참여자 유니크화 + 생성자 추가(본인 자동 참여)
        Set<Long> participantIds = getUniqueParticipantIds(request, userId);
        ChatRoomType chatRoomType = request.chatRoomType();

        if (chatRoomType == ChatRoomType.DIRECT) {
            validateDirectChatRoomCreation(groupId, userId, participantIds);
        }

        String roomName = resolveRoomName(chatRoomType, request.name(), participantIds);

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .description(request.description())
                .chatRoomType(chatRoomType)
                .group(group)
                .createdBy(createdBy)
                .build();
        chatRoomRepository.save(chatRoom);

        // 참가자 저장
        saveParticipants(chatRoom, participantIds);

        return new CreateChatRoomResponse(
                chatRoom.getId().toString(),
                chatRoom.getName(),
                "success"
        );
    }

    /**
     * 채팅방 이름 및 설명을 수정합니다.
     * 생성자만 수정할 수 있습니다.
     */
    @Operation(summary = "채팅방 수정", description = "채팅방 이름과 설명을 수정합니다. 생성자만 수정 가능.")
    @ApiResponse(responseCode = "200", description = "채팅방 수정 성공")
    public UpdateChatRoomResponse updateChatRoom(
            Long groupId,
            Long roomId,
            Long userId,
            UpdateChatRoomRequest req
    ) {
        ChatRoom room = requireValidRoom(groupId, roomId, userId);
        requireCreatorPermission(room, userId);

        room.updateInfo(req.name(), req.description());

        return new UpdateChatRoomResponse(
                room.getId().toString(),
                room.getName(),
                room.getDescription(),
                room.getModifiedAt()
        );
    }

    /**
     * 채팅방을 삭제합니다. 생성자만 삭제할 수 있습니다.
     */
    @Operation(summary = "채팅방 삭제", description = "채팅방을 삭제합니다. 생성자만 삭제 가능.")
    @ApiResponse(responseCode = "200", description = "채팅방 삭제 성공")
    public void deleteChatRoom(Long groupId, Long roomId, Long userId) {
        ChatRoom room = requireValidRoom(groupId, roomId, userId);
        requireCreatorPermission(room, userId);
        chatRoomRepository.delete(room);
    }

    /**
     * 채팅방에 새로운 사용자를 초대합니다.
     */
    @Operation(summary = "참여자 초대", description = "채팅방에 새 사용자를 초대합니다.")
    @ApiResponse(responseCode = "200", description = "참여자 초대 성공")
    public void addParticipant(
            Long groupId,
            Long roomId,
            Long operatorId,
            AddParticipantRequest req
    ) {
        ChatRoom room = requireValidRoom(groupId, roomId, operatorId);

        // 생성자가 아니면 INVITE 권한 검사
        if (!room.getCreatedBy().getId().equals(operatorId)) {
            groupValidator.validatePermission(
                    operatorId,
                    groupId,
                    PermissionType.INVITE_CHAT_PARTICIPANT
            );
        }

        Long newUserId = req.userId();

        groupValidator.validateMember(newUserId, groupId);

        if (chatParticipantRepository.findActiveByRoomAndUser(roomId, newUserId).isPresent()) {
            throw new ChatException(ChatErrorCode.USER_ALREADY_IN_CHAT_ROOM);
        }

        // 사용자 조회
        User newUser = findUserOrThrow(newUserId);

        ChatParticipant cp = ChatParticipant.builder()
                .chatRoom(room)
                .user(newUser)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .lastReadMessageId(0L)
                .build();
        chatParticipantRepository.save(cp);

        broadcastMembersAfterCommit(roomId);
    }

    /**
     * 채팅방에서 특정 사용자를 강퇴합니다.
     */
    @Operation(summary = "참여자 강퇴", description = "채팅방에서 특정 사용자를 강퇴합니다. 생성자 또는 권한 소유자만 가능.")
    @ApiResponse(responseCode = "200", description = "강퇴 성공")
    public void removeParticipant(
            Long groupId,
            Long roomId,
            Long operatorId,
            Long targetUserId
    ) {
        ChatRoom room = requireValidRoom(groupId, roomId, operatorId);

        // 자기 자신을 강퇴할 수 없도록 방어 코드
        if (operatorId.equals(targetUserId)) {
            throw new ChatException(ChatErrorCode.CANNOT_KICK_SELF);
        }

        // 생성자가 아니면 KICK 권한 검사
        if (!room.getCreatedBy().getId().equals(operatorId)) {
            groupValidator.validatePermission(
                    operatorId,
                    groupId,
                    PermissionType.KICK_CHAT_PARTICIPANT
            );
        }

        // 참여자 조회
        ChatParticipant participant =
                chatParticipantRepository
                        .findActiveByRoomAndUser(roomId, targetUserId)
                        .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM));

        // 비활성 처리
        participant.leave();

        broadcastMembersAfterCommit(roomId);
    }

    /**
     * 사용자가 채팅방에서 나갑니다.
     */
    @Operation(summary = "채팅방 나가기", description = "참여자가 채팅방을 나갑니다.")
    @ApiResponse(responseCode = "200", description = "나가기 성공")
    public void leaveChatRoom(Long groupId, Long roomId, Long userId) {
        groupValidator.validateMember(userId, groupId);

        // 채팅방 존재 여부 검증
        ChatRoom room = chatValidator.validateRoom(roomId);
        if (!room.getGroup().getId().equals(groupId)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        // 참여자 검증 및 조회
        ChatParticipant participant =
                chatValidator.validateParticipant(roomId, userId);

        // isActive = false, leftAt 세팅
        participant.leave();

        // 커밋 후 브로드캐스트
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        chatMemberEventService.broadcastMembers(roomId);
                    }
                }
        );
    }

    // -------------------- Private Helpers --------------------

    /** 유저 조회 및 예외 통일 */
    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));
    }

    /** 참여자 중복 제거 및 생성자 추가 */
    private Set<Long> getUniqueParticipantIds(CreateChatRoomRequest request, Long creatorId) {
        Set<Long> ids = new HashSet<>(Optional.ofNullable(request.participantIds()).orElse(Collections.emptyList()));
        ids.add(creatorId);
        return ids;
    }

    /**
     * 그룹 멤버 검증 → 채팅방 존재 & 그룹 일치 검증을 수행하고
     * 검증된 ChatRoom 을 반환
     */
    private ChatRoom requireValidRoom(Long groupId, Long roomId, Long userId) {
        // 그룹 멤버 검증
        groupValidator.validateMember(userId, groupId);

        // 방 존재 여부 검증
        ChatRoom room = chatValidator.validateRoom(roomId);

        // 그룹 일치 검증
        if (!room.getGroup().getId().equals(groupId)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        return room;
    }

    /**
     * room 생성자 권한 검증
     */
    private void requireCreatorPermission(ChatRoom room, Long userId) {
        if (!room.getCreatedBy().getId().equals(userId)) {
            throw new ChatException(ChatErrorCode.NO_PERMISSION);
        }
    }

    /**
     * 트랜잭션 커밋 후 브로드캐스트
     */
    private void broadcastMembersAfterCommit(Long roomId) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        chatMemberEventService.broadcastMembers(roomId);
                    }
                }
        );
    }

    /** DIRECT 방 생성 검증 및 중복 방지 */
    private void validateDirectChatRoomCreation(Long groupId, Long userId, Set<Long> ids) {
        groupValidator.validatePermission(userId, groupId, PermissionType.CREATE_DIRECT_CHAT_ROOM);
        if (ids.size() != 2) throw new ChatException(ChatErrorCode.INVALID_PARTICIPANT_COUNT);

        List<ChatRoom> existing = chatRoomRepository.findDirectRoomByParticipants(ids.stream().sorted().toList(), ids.size());
        if (!existing.isEmpty()) throw new ChatException(ChatErrorCode.DUPLICATE_DIRECT_ROOM);
    }

    /** 채팅방 이름 설정 */
    private String resolveRoomName(ChatRoomType type, String defaultName, Set<Long> participantIds) {
        if (type == ChatRoomType.DIRECT) {
            return generateDirectRoomName(participantIds.stream().toList());
        }
        return defaultName;
    }

    /** DIRECT 방 이름 생성 */
    private String generateDirectRoomName(List<Long> participantIds) {
        List<User> participants = userRepository.findAllById(participantIds);
        return participants.stream()
                .map(User::getName)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("DIRECT");
    }

    /** 채팅방 참가자 등록 */
    private void saveParticipants(ChatRoom chatRoom, Set<Long> participantIds) {
        // 각 참가자 엔티티 저장
        for (Long participantId : participantIds) {
            User participant = findUserOrThrow(participantId);
            ChatParticipant participantEntity = ChatParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(participant)
                    .joinedAt(LocalDateTime.now())
                    .isActive(true)
                    .lastReadMessageId(0L)
                    .build();
            chatParticipantRepository.save(participantEntity);
        }

        //  트랜잭션 커밋 이후 채팅방 멤버 전체 갱신 브로드캐스트
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                chatMemberEventService.broadcastMembers(chatRoom.getId());
            }
        });
    }

}
