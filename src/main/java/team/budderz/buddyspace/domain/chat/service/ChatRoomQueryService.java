package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.response.ChatRoomSummaryResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.repository.ChatMessageRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.membership.repository.MembershipRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MembershipRepository membershipRepository;

    public List<ChatRoomSummaryResponse> getMyChatRooms(Long groupId, Long userId) {

        boolean isMember = membershipRepository.existsByUser_IdAndGroup_Id(userId, groupId);
        if (!isMember) {
            throw new ChatException(ChatErrorCode.USER_NOT_IN_GROUP);
        }

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
}