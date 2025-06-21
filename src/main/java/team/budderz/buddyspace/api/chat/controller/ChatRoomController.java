package team.budderz.buddyspace.api.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.chat.request.rest.CreateChatRoomRequest;
import team.budderz.buddyspace.api.chat.request.rest.UpdateChatRoomRequest;
import team.budderz.buddyspace.api.chat.response.rest.*;
import team.budderz.buddyspace.domain.chat.service.ChatRoomServiceFacade;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

/* 채팅방 CRUD */
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

    // 채팅방 수정 -----------------------------------------------------------------------------------------------------
    @PatchMapping("/{roomId}")
    public BaseResponse<UpdateChatRoomResponse> updateChatRoom(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateChatRoomRequest req
    ) {
        Long userId = userAuth.getUserId();
        UpdateChatRoomResponse res = chatRoomService.updateChatRoom(groupId, roomId, userId, req);
        return new BaseResponse<>(res);
    }

    // 채팅방 삭제  -----------------------------------------------------------------------------------------------------
    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChatRoom(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId
    ) {
        Long userId = userAuth.getUserId();
        chatRoomService.deleteChatRoom(groupId, roomId, userId);
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

    // 단일 채팅방 조회 -----------------------------------------------------------------------------------------------------
    @GetMapping("/{roomId}")
    public BaseResponse<ChatRoomDetailResponse> getChatRoomDetail(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId
    ) {
        Long userId = userAuth.getUserId();
        ChatRoomDetailResponse room = chatRoomService.getChatRoomDetail(groupId, roomId, userId);
        return new BaseResponse<>(room);
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

    // 멤버 목록 조회 -----------------------------------------------------------------------------------------------------
    @GetMapping("/{roomId}/members")
    public BaseResponse<List<ChatRoomMemberResponse>> getChatRoomMembers(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId
    ) {
        Long userId = userAuth.getUserId();
        // 권한 검사: 내가 이 그룹/방의 멤버인지
        // (서비스에서 validateMember 등 체크)
        List<ChatRoomMemberResponse> members = chatRoomService.getChatRoomMembers(groupId, roomId, userId);
        return new BaseResponse<>(members);
    }

    // 읽음 상태 조회 -----------------------------------------------------------------------------------------------------
    @GetMapping("/{roomId}/read-status")
    public BaseResponse<ReadStatusRestResponse> getReadStatus(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId
    ) {
        Long userId = userAuth.getUserId();
        ReadStatusRestResponse status =
                chatRoomService.getReadStatus(groupId, roomId, userId);
        return new BaseResponse<>(status);
    }
}
