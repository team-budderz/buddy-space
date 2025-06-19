package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.response.rest.*;
import team.budderz.buddyspace.api.chat.response.ws.ChatMessageResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.domain.chat.validator.ChatValidator;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.repository.ChatMessageRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MembershipRepository membershipRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final GroupValidator groupValidator;
    private final ChatValidator chatValidator;

    // 채팅방 목록 조회 -------------------------------------------------------------------------------------------------
    public List<ChatRoomSummaryResponse> getMyChatRooms(Long groupId, Long userId) {

        // 그룹 멤버 검증
        groupValidator.validateMember(userId, groupId);

        List<ChatParticipant> participants = chatParticipantRepository
                .findByUserAndGroupAndIsActive(userId, groupId);

        return participants.stream()
                .map(this::toChatRoomSummaryResponse)
                .toList();
    }

    private ChatRoomSummaryResponse toChatRoomSummaryResponse(ChatParticipant participant) {
        ChatRoom chatRoom = participant.getChatRoom();
        ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderBySentAtDesc(chatRoom);

        long lastReadId = getLastReadMessageIdOrDefault(participant);

        long unreadCount = chatMessageRepository.countByChatRoomAndIdGreaterThan(chatRoom, lastReadId);

        return new ChatRoomSummaryResponse(
                chatRoom.getId(),
                chatRoom.getName(),
                lastMessage != null ? lastMessage.getContent() : "",
                lastMessage != null ? lastMessage.getMessageType().name() : "TEXT",
                lastMessage != null ? lastMessage.getSentAt() : null,
                unreadCount
        );
    }

    private long getLastReadMessageIdOrDefault(ChatParticipant participant) {
        return participant.getLastReadMessageId();
    }

    // 채팅방 입장 후 과거 메시지 조회 -------------------------------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public GetChatMessagesResponse getChatMessages(Long groupId, Long roomId, Long userId, int page, int size) {

        groupValidator.validateMember(userId, groupId);

        // 채팅방 참여자인지 확인
        ChatParticipant participant = chatParticipantRepository
                .findByUserAndChatRoom_IdAndChatRoom_Group_IdAndIsActiveTrue(userId, roomId, groupId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM));

        // 메시지 페이징 조회
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt")); // 최신순
        Page<ChatMessage> messagePage = chatMessageRepository.findByChatRoom_Id(roomId, pageable);

        //  DTO 변환
        List<ChatMessageResponse> messages = messagePage.stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getId(),
                        msg.getSender().getId(),
                        msg.getMessageType().name(),
                        msg.getContent(),
                        msg.getAttachmentUrl(),
                        msg.getSentAt()
                ))
                .toList();

        return new GetChatMessagesResponse(
                messages,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalPages(),
                messagePage.getTotalElements()
        );
    }

    // 채팅방 멤버 목록 조회 -------------------------------------------------------------------------------------------------
    public List<ChatRoomMemberResponse> getChatRoomMembers(Long groupId, Long roomId, Long userId) {
        // 방, 그룹, 멤버 유효성 검사
        chatValidator.validateRoom(roomId);
        chatValidator.validateParticipant(roomId, userId);

        // 활성화된 멤버만 반환
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(room);
        return participants.stream()
                .filter(ChatParticipant::isActive)
                .sorted(Comparator.comparing(cp -> cp.getUser().getName()))
                .map(cp -> {
                    var user = cp.getUser();
                    return new ChatRoomMemberResponse(
                            user.getId(),
                            user.getName(),
                            user.getImageUrl()
                    );
                })
                .toList();
    }

    // 단일 방 조회 -------------------------------------------------------------------------------------------------
    public ChatRoomDetailResponse getChatRoomDetail(Long groupId, Long roomId, Long userId) {
        groupValidator.validateMember(userId, groupId);
        ChatRoom room = chatValidator.validateRoom(roomId);

        if (!room.getGroup().getId().equals(groupId)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        // 자기 자신도 그 방의 멤버여야 한다면 추가 검증
        chatValidator.validateParticipant(roomId, userId);

        // 필요한 정보 취합
        int count = chatRoomRepository.countActiveParticipants(roomId);
        return new ChatRoomDetailResponse(
                room.getId().toString(),
                room.getName(),
                room.getDescription(),
                room.getChatRoomType(),
                room.getCreatedBy().getId(),
                room.getCreatedAt(),
                count
        );
    }

    // 읽음 상태 조회 (보완용)  -------------------------------------------------------------------------------------------------
    public ReadStatusRestResponse getReadStatus(Long groupId, Long roomId, Long userId) {
        // 그룹 멤버 + 방 검증
        groupValidator.validateMember(userId, groupId);
        ChatRoom room = chatValidator.validateRoom(roomId);
        if (!room.getGroup().getId().equals(groupId)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        // 내 읽음 상태
        ChatParticipant me = chatParticipantRepository
                .findActiveByRoomAndUser(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM));
        Long myLastRead = me.getLastReadMessageId();

        // 내 unread count
        int unreadCount = (int) chatMessageRepository.countByChatRoom_IdAndIdGreaterThan(roomId, myLastRead);

        // 모든 참가자 상태
        List<ReadStatusRestResponse.ParticipantReadStatus> participants =
                chatParticipantRepository.findActiveByRoom(roomId).stream()
                        .map(p -> new ReadStatusRestResponse.ParticipantReadStatus(
                                p.getUser().getId(),
                                p.getLastReadMessageId()
                        ))
                        .toList();

        return new ReadStatusRestResponse(roomId, myLastRead, unreadCount, participants);
    }


}