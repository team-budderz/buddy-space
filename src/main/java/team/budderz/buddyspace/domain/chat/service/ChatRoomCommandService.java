package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoomType;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // 채팅방 생성 -------------------------------------------------------------------------------------------------------
    public CreateChatRoomResponse createChatRoom(Long groupId, Long userId, CreateChatRoomRequest request) {
        // 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 생성자(유저) 조회
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));

        // 그룹 참여 여부 확인
        boolean isMember = membershipRepository.existsByUser_IdAndGroup_Id(userId, groupId);
        if (!isMember) {
            throw new ChatException(ChatErrorCode.USER_NOT_IN_GROUP);
        }

        // 동일한 참여자 중복 제거(Set) + 생성자 본인 추가(본인 자동 참여)
        Set<Long> uniqueParticipantIds = new HashSet<>(request.participantIds());
        uniqueParticipantIds.add(userId);

        ChatRoomType chatRoomType = request.chatRoomType();

        // DIRECT 방이면 중복 체크
        if (chatRoomType == ChatRoomType.DIRECT) {
            // 참여자 수 2명만 허용 (보통 DIRECT 1:1)
            if (uniqueParticipantIds.size() != 2) {
                throw new ChatException(ChatErrorCode.INVALID_PARTICIPANT_COUNT);
            }

            // 기존 DIRECT 방 조회 (참여자 조합 기준)
            List<Long> participantIdList = uniqueParticipantIds.stream()
                    .sorted()
                    .toList();

            List<ChatRoom> existingRooms = chatRoomRepository.findDirectRoomByParticipants(participantIdList, participantIdList.size());

            if (!existingRooms.isEmpty()) {
                // 이미 존재 → 첫 번째 방 반환
                ChatRoom existingRoom = existingRooms.get(0);
                throw new ChatException(ChatErrorCode.DUPLICATE_DIRECT_ROOM);
            }
        }

        // 방 이름 설정
        String roomName = request.name();
        if (chatRoomType == ChatRoomType.DIRECT) {
            roomName = generateDirectRoomName(uniqueParticipantIds.stream().toList());
        }

        // ChatRoom 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .description(request.description())
                .chatRoomType(chatRoomType)
                .group(group)
                .createdBy(createdBy)
                .build();

        chatRoomRepository.save(chatRoom);

        // ChatParticipant 저장 (참여자 등록)
        for (Long participantId : uniqueParticipantIds) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));

            ChatParticipant participantEntity = ChatParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(participant)
                    .joinedAt(LocalDateTime.now())
                    .isActive(true)
                    .lastReadMessageId(null)
                    .build();

            chatParticipantRepository.save(participantEntity);
        }

        log.info("userId = {}", userId);
        log.info("groupId = {}", groupId);
        log.info("isMember = {}", isMember);

        return new CreateChatRoomResponse(chatRoom.getId().toString(), chatRoom.getName(), "success");
    }

    private String generateDirectRoomName(List<Long> participantIds) {
        List<User> participants = userRepository.findAllById(participantIds);
        return participants.stream()
                .map(User::getName)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("DIRECT");
    }
}