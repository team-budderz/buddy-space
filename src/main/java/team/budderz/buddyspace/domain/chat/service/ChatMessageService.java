package team.budderz.buddyspace.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.chat.request.ChatMessageSendRequest;
import team.budderz.buddyspace.api.chat.response.ChatMessageResponse;
import team.budderz.buddyspace.domain.chat.exception.ChatErrorCode;
import team.budderz.buddyspace.domain.chat.exception.ChatException;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import team.budderz.buddyspace.infra.database.chat.entity.MessageType;
import team.budderz.buddyspace.infra.database.chat.repository.ChatMessageRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatParticipantRepository;
import team.budderz.buddyspace.infra.database.chat.repository.ChatRoomRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;

    // 메시지 저장 -------------------------------------------------------------------------------------------------------
    public ChatMessageResponse saveChatMessage(ChatMessageSendRequest request) {
        // 채팅방 존재 + 유저 확인 및 조회
        ChatRoom chatRoom = getChatRoomOrThrow(request.roomId());
        User sender = getUserOrThrow(request.senderId());

        // 채팅방 참여자인지 검증
        assertParticipantOrThrow(sender.getId(), chatRoom);

        // 메시지 저장
        ChatMessage chatMessage = buildChatMessage(request, chatRoom, sender);
        chatMessageRepository.save(chatMessage);

        return toChatMessageResponse(chatMessage);
    }

    /** 채팅방 조회 및 예외 */
    private ChatRoom getChatRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    /** 유저 조회 및 예외 */
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));
    }

    /**
     * 채팅방 참여자 검증 (정책: 채팅방+그룹 기준, isActive=true)
     */
    private void assertParticipantOrThrow(Long userId, ChatRoom chatRoom) {
        boolean exists = chatParticipantRepository
                .findByUserAndChatRoom_IdAndChatRoom_Group_IdAndIsActiveTrue(
                        userId,
                        chatRoom.getId(),
                        chatRoom.getGroup().getId()
                )
                .isPresent();
        if (!exists) {
            throw new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM);
        }
    }

    /** ChatMessage 엔티티 빌드 */
    private ChatMessage buildChatMessage(ChatMessageSendRequest request, ChatRoom chatRoom, User sender) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(MessageType.valueOf(request.messageType()))
                .content(request.content())
                .attachmentUrl(request.attachmentUrl())
                .sentAt(LocalDateTime.now())
                .build();
    }

    /** DTO 변환 */
    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getSender().getId(),
                chatMessage.getMessageType().name(),
                chatMessage.getContent(),
                chatMessage.getAttachmentUrl(),
                chatMessage.getSentAt()
        );
    }
}
