package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoomType;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// 방 조회
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    // 방 생성
    public CreateChatRoomResponse createChatRoom(Long groupId, Long userId, CreateChatRoomRequest request) {
        // 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        // 생성자(유저) 조회
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 동일한 참여자 중복 제거(Set) + 생성자 본인 추가(본인 자동 참여)
        Set<Long> uniqueParticipantIds = new java.util.HashSet<>(request.getParticipantIds());
        uniqueParticipantIds.add(userId);

        // DIRECT 방이면 중복 체크
        if (ChatRoomType.valueOf(request.getChatRoomType()) == ChatRoomType.DIRECT) {

            // 참여자 수 2명만 허용 (보통 DIRECT 1:1)
            if (uniqueParticipantIds.size() != 2) {
                throw new IllegalArgumentException("DIRECT 채팅방은 정확히 2명의 참여자가 필요합니다.");
            }

            // 기존 DIRECT 방 조회 (참여자 조합 기준)
            List<Long> participantIdList = uniqueParticipantIds.stream().sorted().toList();

            List<ChatRoom> existingRooms = chatRoomRepository.findDirectRoomByParticipants(participantIdList,  participantIdList.size());

            if (!existingRooms.isEmpty()) {
                // 이미 존재 → 첫 번째 방 반환
                ChatRoom existingRoom = existingRooms.get(0);
                return new CreateChatRoomResponse(existingRoom.getId().toString(), existingRoom.getName(), "already_exists");
            }
        }

        // 방 이름 설정
        String roomName = request.getName();
        if (ChatRoomType.valueOf(request.getChatRoomType()) == ChatRoomType.DIRECT) {
            roomName = generateDirectRoomName(uniqueParticipantIds.stream().toList());
        }

        // ChatRoom 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
                .description(request.getDescription())
                .chatRoomType(ChatRoomType.valueOf(request.getChatRoomType()))
                .group(group)
                .createdBy(createdBy)
                .build();

        chatRoomRepository.save(chatRoom);

        // ChatParticipant 저장 (참여자 등록)
        for (Long participantId : uniqueParticipantIds) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + participantId));

            ChatParticipant participantEntity = ChatParticipant.builder()
                    .chatRoom(chatRoom)
                    .user(participant)
                    .joinedAt(LocalDateTime.now())
                    .isActive(true)
                    .lastReadMessageId(null)
                    .build();

            chatParticipantRepository.save(participantEntity);
        }


        return new CreateChatRoomResponse(chatRoom.getId().toString(), chatRoom.getName(), "success");
    }

    // DIRECT 방 이름 자동 생성
    private String generateDirectRoomName(List<Long> participantIds) {
        List<User> participants = userRepository.findAllById(participantIds);
        return participants.stream()
                .map(User::getName)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("DIRECT");
    }

}
