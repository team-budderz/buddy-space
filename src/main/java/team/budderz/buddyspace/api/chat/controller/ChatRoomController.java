package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.ChatRoomSummaryResponse;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;
import team.budderz.buddyspace.api.chat.response.GetChatMessagesResponse;
import team.budderz.buddyspace.domain.chat.service.ChatRoomCommandService;
import team.budderz.buddyspace.domain.chat.service.ChatRoomServiceFacade;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

// REST API: 방 생성, 방 조회 등
@RestController
@RequestMapping("/api/group/{groupId}/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomServiceFacade chatRoomService;

    // 채팅방 생성 -----------------------------------------------------------------------------------------------------
    @PostMapping
    public BaseResponse<CreateChatRoomResponse> createChatRoom(
             @AuthenticationPrincipal UserAuth userAuth,
             @PathVariable Long groupId,
             @RequestBody CreateChatRoomRequest request
             ) {

         Long userId = userAuth.getUserId();

         CreateChatRoomResponse createChatRoomResponse = chatRoomService.createChatRoom(groupId, userId, request);
         return new BaseResponse<>(createChatRoomResponse);
     }

     // 채팅방 목록 조회 -----------------------------------------------------------------------------------------------------
     @GetMapping("/my")
     public BaseResponse<List<ChatRoomSummaryResponse>> getMyChatRooms(
             @AuthenticationPrincipal UserAuth userAuth,
             @PathVariable Long groupId
     ) {
         Long userId = userAuth.getUserId();
         List<ChatRoomSummaryResponse> rooms = chatRoomService.getMyChatRooms(groupId, userId);
         return new BaseResponse<>(rooms);
     }

    // 채팅방 입장 후 과거 메시지 조회 -----------------------------------------------------------------------------------------------------
    @GetMapping("/{roomId}/messages")
    public BaseResponse<GetChatMessagesResponse> getChatMessages(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = userAuth.getUserId();
        GetChatMessagesResponse response = chatRoomService.getChatMessages(groupId, roomId, userId, page, size);
        return new BaseResponse<>(response);
    }

}
