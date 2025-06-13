package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
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

// 생성, 수정 등 "쓰기" 성향
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatRoomCommandService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final GroupValidator groupValidator;

    public CreateChatRoomResponse createChatRoom(Long groupId, Long userId, CreateChatRoomRequest request) {
        // 그룹, 유저, 멤버 검증
        Group group = groupValidator.findGroupOrThrow(groupId);
        User createdBy = findUserOrThrow(userId);
        groupValidator.validateMember(userId, groupId);

        // 참여자 유니크화 + 생성자 추가(본인 자동 참여)
        Set<Long> participantIds = getUniqueParticipantIds(request, userId);

        ChatRoomType chatRoomType = request.chatRoomType();

        if (chatRoomType == ChatRoomType.DIRECT) {
            validateDirectChatRoomCreation(userId, groupId, participantIds);
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

        log.info("ChatRoom 생성 완료 - userId: {}, groupId: {}, roomId: {}", userId, groupId, chatRoom.getId());

        return new CreateChatRoomResponse(
                chatRoom.getId().toString(),
                chatRoom.getName(),
                "success"
        );
    }

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

    /** DIRECT 방 생성 검증 및 중복 방지 */
    private void validateDirectChatRoomCreation(Long userId, Long groupId, Set<Long> participantIds) {
        groupValidator.validatePermission(userId, groupId, PermissionType.CREATE_DIRECT_CHAT_ROOM);

        if (participantIds.size() != 2) {
            throw new ChatException(ChatErrorCode.INVALID_PARTICIPANT_COUNT);
        }

        List<Long> participantIdList = participantIds.stream().sorted().toList();
        List<ChatRoom> existingRooms = chatRoomRepository.findDirectRoomByParticipants(participantIdList, participantIdList.size());
        if (!existingRooms.isEmpty()) {
            throw new ChatException(ChatErrorCode.DUPLICATE_DIRECT_ROOM);
        }
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
    }
}
