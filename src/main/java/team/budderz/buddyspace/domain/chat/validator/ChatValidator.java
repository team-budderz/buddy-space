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

/**
 * 채팅 도메인 전반에 걸친 유효성 검증을 담당하는 컴포넌트입니다.
 * <ul>
 *     <li>채팅방 존재 여부</li>
 *     <li>메시지의 유효성과 소속 방 일치 여부</li>
 *     <li>사용자의 채팅방 참여 여부</li>
 *     <li>읽기 권한 확인</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class ChatValidator {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * 채팅방의 존재 여부를 검증합니다.
     *
     * @param roomId 채팅방 ID
     * @return 존재하는 {@link ChatRoom} 엔티티
     * @throws ChatException 채팅방이 존재하지 않을 경우 {@link ChatErrorCode#CHAT_ROOM_NOT_FOUND} 예외 발생
     */
    public ChatRoom validateRoom(Long roomId) {
         return chatRoomRepository.findById(roomId)
                 .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        }

    /**
     * 메시지가 존재하고 주어진 채팅방에 속해 있는지 검증합니다.
     *
     * @param messageId 메시지 ID
     * @param roomId 채팅방 ID
     * @return 해당 {@link ChatMessage} 엔티티
     * @throws ChatException 메시지가 존재하지 않거나, 다른 방에 속할 경우 예외 발생
     * <ul>
     *     <li>{@link ChatErrorCode#MESSAGE_NOT_FOUND}</li>
     *     <li>{@link ChatErrorCode#MESSAGE_NOT_IN_ROOM}</li>
     * </ul>
     */
    public ChatMessage validateMessageInRoom(Long messageId, Long roomId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.MESSAGE_NOT_FOUND));
        if (!message.getChatRoom().getId().equals(roomId)) {
            throw new ChatException(ChatErrorCode.MESSAGE_NOT_IN_ROOM);
        }
        return message;
    }

    /**
     * 사용자가 주어진 채팅방에 참여 중인지 검증합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 유효한 {@link ChatParticipant} 엔티티
     * @throws ChatException 유저가 해당 채팅방에 참여하고 있지 않으면 예외 발생
     * <ul>
     *     <li>{@link ChatErrorCode#USER_NOT_IN_CHAT_ROOM}</li>
     * </ul>
     */
    public ChatParticipant validateParticipant(Long roomId, Long userId) {
        return chatParticipantRepository.findActiveByRoomAndUser(roomId, userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM));
    }

    /**
     * 메시지 읽기 권한을 검증합니다.
     * 기본적으로 채팅방에 참여 중인 사용자라면 읽기 권한이 있다고 간주합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @param message 읽으려는 메시지
     * @throws ChatException 읽기 권한이 없을 경우 예외 발생 (현재는 {@link #validateParticipant}로 검증 대체)
     */
    public void validateReadPermission(Long roomId, Long userId, ChatMessage message) {
        // 정책에 따라 읽기 제한이 필요하면 추가, 기본은 참가자면 모두 읽기 가능
        validateParticipant(roomId, userId);
    }
}

