package team.budderz.buddyspace.domain.chat.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.repository.ChatMessageRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;

@Component
@RequiredArgsConstructor
public class ChatValidator {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    /** 채팅방 존재 여부 검증 */
    public ChatRoom validateRoom(Long roomId) {
         return chatRoomRepository.findById(roomId)
                 .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        }

    /** 메시지가 실제 존재하고 해당 roomId에 속하는지 검증 및 반환 */
    public ChatMessage validateMessageInRoom(Long messageId, Long roomId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.MESSAGE_NOT_FOUND));
        if (!message.getChatRoom().getId().equals(roomId)) {
            throw new ChatException(ChatErrorCode.MESSAGE_NOT_IN_ROOM);
        }
        return message;
    }

    /** 채팅방 참가자 여부 검증 */
    public ChatParticipant validateParticipant(Long roomId, Long userId) {
        return chatParticipantRepository.findActiveByRoomAndUser(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM));
    }

    /** 메시지 읽기 권한 체크 (정책에 따라 추가 구현 가능) */
    public void validateReadPermission(Long roomId, Long userId, ChatMessage message) {
        // 정책에 따라 읽기 제한이 필요하면 추가, 기본은 참가자면 모두 읽기 가능
        validateParticipant(roomId, userId);
    }
}

