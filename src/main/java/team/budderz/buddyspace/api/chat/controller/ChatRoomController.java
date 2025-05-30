package team.budderz.buddyspace.api.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.budderz.buddyspace.api.chat.request.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.CreateChatRoomResponse;
import team.budderz.buddyspace.domain.chat.service.ChatRoomService;
import team.budderz.buddyspace.global.response.BaseResponse;

// REST API: 방 생성, 방 조회 등
@RestController
@RequestMapping("/api/group/groupId}/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

     public BaseResponse<CreateChatRoomResponse> createChatRoom(
             @PathVariable Long groupId,
             @RequestBody CreateChatRoomRequest request
             ) {

         Long userId = 1L; // TODO: 실제 로그인 사용자 ID로 변경
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//         CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//         Long userId = userDetails.getUserId();

         CreateChatRoomResponse createChatRoomResponse = chatRoomService.createChatRoom(groupId, userId, request);
         return new BaseResponse<>(createChatRoomResponse);
     }
}
