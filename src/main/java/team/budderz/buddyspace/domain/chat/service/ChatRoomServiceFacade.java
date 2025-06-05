package team.budderz.buddyspace.domain.chat.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.ChatRoomSummaryResponse;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;

import java.util.List;

// Facade 가 트랜잭션 경계를 잡고 Command/Query 를 호출
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomServiceFacade {

    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomQueryService chatRoomQueryService;

    // 채팅방 생성 (쓰기)
    @Transactional
    public CreateChatRoomResponse createChatRoom(Long groupId, Long userId, CreateChatRoomRequest request) {
        return chatRoomCommandService.createChatRoom(groupId, userId, request);
    }

    // 채팅방 목록 조회 (읽기)
    @Transactional(readOnly = true)
    public List<ChatRoomSummaryResponse> getMyChatRooms(Long groupId, Long userId) {
        return chatRoomQueryService.getMyChatRooms(groupId, userId);
    }
}
