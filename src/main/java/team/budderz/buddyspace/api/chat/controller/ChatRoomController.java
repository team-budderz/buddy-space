package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.ChatRoomSummaryResponse;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;
import team.budderz.buddyspace.domain.chat.service.ChatRoomService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

// REST API: 방 생성, 방 조회 등
@RestController
@RequestMapping("/api/group/{groupId}/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 채팅방 생성
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

     // 채팅방 목록 조회
     @GetMapping("/my")
     public BaseResponse<List<ChatRoomSummaryResponse>> getMyChatRooms(
             @AuthenticationPrincipal UserAuth userAuth,
             @PathVariable Long groupId
     ) {
         Long userId = userAuth.getUserId();
         List<ChatRoomSummaryResponse> rooms = chatRoomService.getMyChatRooms(groupId, userId);
         return new BaseResponse<>(rooms);
     }


}
