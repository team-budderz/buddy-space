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
        // 채팅방 존재 여부 확인 및 조회
        ChatRoom chatRoom = findChatRoomOrThrow(request.roomId());

        // 유저 존재 여부 확인 및 조회
        User sender = findUserOrThrow(request.senderId());

        // 채팅방 참여자인지 검증
        validateParticipant(sender.getId(), chatRoom);

        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(MessageType.valueOf(request.messageType()))
                .content(request.content())
                .sentAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);

        // 저장된 메시지 → DTO 반환
        return new ChatMessageResponse(
                chatMessage.getId(),
                sender.getId(),
                chatMessage.getMessageType().name(),
                chatMessage.getContent(),
                chatMessage.getAttachmentUrl(),
                chatMessage.getSentAt()
        );
    }

    // 채팅방 존재 확인
    private ChatRoom findChatRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    // 유저 존재 확인
    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_FOUND));
    }

    // 채팅방 참여자 검증
    private void validateParticipant(Long userId, ChatRoom chatRoom) {
        chatParticipantRepository.findByUserAndChatRoom_IdAndChatRoom_Group_IdAndIsActiveTrue(
                userId,
                chatRoom.getId(),
                chatRoom.getGroup().getId()
        ).orElseThrow(() -> new ChatException(ChatErrorCode.USER_NOT_IN_CHAT_ROOM));
    }
}
