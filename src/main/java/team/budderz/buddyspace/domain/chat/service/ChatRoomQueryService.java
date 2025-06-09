package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.response.ChatMessageResponse;
import team.budderz.buddyspace.api.chat.response.ChatRoomSummaryResponse;
import team.budderz.buddyspace.api.chat.response.GetChatMessagesResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.repository.ChatMessageRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MembershipRepository membershipRepository;
    private final GroupValidator groupValidator;

    // 채팅방 목록 조회 -------------------------------------------------------------------------------------------------
    public List<ChatRoomSummaryResponse> getMyChatRooms(Long groupId, Long userId) {

        // 그룹 멤버인지 확인
        groupValidator.validateMember(userId, groupId);

        List<ChatParticipant> participants = chatParticipantRepository
                .findByUserAndGroupAndIsActive(userId, groupId);

        return participants.stream()
                .map(participant -> {
                    ChatRoom chatRoom = participant.getChatRoom();
                    ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderBySentAtDesc(chatRoom);

                    long lastReadId = participant.getLastReadMessageId() != null ? participant.getLastReadMessageId() : 0L;

                    long unreadCount = chatMessageRepository.countByChatRoomAndIdGreaterThan(chatRoom, lastReadId);

                    return new ChatRoomSummaryResponse(
                            chatRoom.getId(),
                            chatRoom.getName(),
                            lastMessage != null ? lastMessage.getContent() : "",
                            lastMessage != null ? lastMessage.getMessageType().name() : "TEXT",
                            lastMessage != null ? lastMessage.getSentAt() : null,
                            unreadCount
                    );
                })
                .toList();
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

}