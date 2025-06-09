package team.budderz.buddyspace.domain.chat.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.ChatRoomSummaryResponse;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;
import team.budderz.buddyspace.api.chat.response.GetChatMessagesResponse;

import java.util.List;

// Facade 가 트랜잭션 경계를 잡고 Command/Query 를 호출
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceFacade {

    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomQueryService chatRoomQueryService;

    // 채팅방 생성
    @Transactional
    public CreateChatRoomResponse createChatRoom(Long groupId, Long userId, CreateChatRoomRequest request) {
        return chatRoomCommandService.createChatRoom(groupId, userId, request);
    }

    // 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomSummaryResponse> getMyChatRooms(Long groupId, Long userId) {
        return chatRoomQueryService.getMyChatRooms(groupId, userId);
    }

    // 채팅방 입장 후 과거 매시지 조회
    @Transactional(readOnly = true)
    public GetChatMessagesResponse getChatMessages(Long groupId, Long roomId, Long userId, int page, int size) {
        return chatRoomQueryService.getChatMessages(groupId, roomId, userId, page, size);
    }

}
