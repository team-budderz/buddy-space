package team.budderz.buddyspace.api.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * 채팅방 생성, 수정, 삭제 및 조회를 처리하는 REST API 컨트롤러입니다.
 */
@Tag(name = "채팅방 관리", description = "채팅방 생성, 수정, 삭제 및 참여자 관리 API")
@RestController
@RequestMapping("/api/group/{groupId}/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomServiceFacade chatRoomService;

    // 채팅방 생성 -----------------------------------------------------------------------------------------------------
    @Operation(
            summary = "채팅방 생성",
            description = "새로운 채팅방을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "채팅방 생성 성공"),
            }
    )
    @PostMapping
    public BaseResponse<CreateChatRoomResponse> createChatRoom(
             @Parameter(hidden = true) @AuthenticationPrincipal UserAuth userAuth,
             @PathVariable Long groupId,
             @RequestBody CreateChatRoomRequest request
             ) {

         Long userId = userAuth.getUserId();

         CreateChatRoomResponse createChatRoomResponse = chatRoomService.createChatRoom(groupId, userId, request);
         return new BaseResponse<>(createChatRoomResponse);
     }

    // 채팅방 수정 -----------------------------------------------------------------------------------------------------
    @Operation(
            summary = "채팅방 수정",
            description = "기존 채팅방의 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "채팅방 수정 성공"),
            }
    )
    @PatchMapping("/{roomId}")
    public BaseResponse<UpdateChatRoomResponse> updateChatRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateChatRoomRequest req
    ) {
        Long userId = userAuth.getUserId();
        UpdateChatRoomResponse res = chatRoomService.updateChatRoom(groupId, roomId, userId, req);
        return new BaseResponse<>(res);
    }

    // 채팅방 삭제  -----------------------------------------------------------------------------------------------------
    @Operation(
            summary = "채팅방 삭제",
            description = "채팅방을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
            }
    )
    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChatRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId
    ) {
        Long userId = userAuth.getUserId();
        chatRoomService.deleteChatRoom(groupId, roomId, userId);
    }

     // 나의 채팅방 목록 조회 -----------------------------------------------------------------------------------------------------
     @Operation(
             summary = "나의 채팅방 목록 조회",
             description = "현재 사용자가 참여 중인 채팅방 목록을 조회합니다."
     )

     @GetMapping("/my")
     public BaseResponse<List<ChatRoomSummaryResponse>> getMyChatRooms(
             @Parameter(hidden = true) @AuthenticationPrincipal UserAuth userAuth,
             @PathVariable Long groupId
     ) {
         Long userId = userAuth.getUserId();
         List<ChatRoomSummaryResponse> rooms = chatRoomService.getMyChatRooms(groupId, userId);
         return new BaseResponse<>(rooms);
     }

    // 채팅방 상세 조회 -----------------------------------------------------------------------------------------------------
    @Operation(
            summary = "채팅방 상세 조회",
            description = "특정 채팅방의 상세 정보를 조회합니다."
    )
    @GetMapping("/{roomId}")
    public BaseResponse<ChatRoomDetailResponse> getChatRoomDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId
    ) {
        Long userId = userAuth.getUserId();
        ChatRoomDetailResponse room = chatRoomService.getChatRoomDetail(groupId, roomId, userId);
        return new BaseResponse<>(room);
    }

    // 채팅방 입장 후 과거 메시지 조회 -----------------------------------------------------------------------------------------------------
    @Operation(
            summary = "채팅 메시지 목록 조회",
            description = "채팅방의 메시지를 페이지 단위로 조회합니다."
    )
    @GetMapping("/{roomId}/messages")
    public BaseResponse<GetChatMessagesResponse> getChatMessages(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = userAuth.getUserId();
        GetChatMessagesResponse response = chatRoomService.getChatMessages(groupId, roomId, userId, page, size);
        return new BaseResponse<>(response);
    }

    // 채팅방 참여자 목록 조회 -----------------------------------------------------------------------------------------------------
    @Operation(
            summary = "채팅방 참여자 목록 조회",
            description = "채팅방에 참여 중인 사용자 목록을 조회합니다."
    )
    @GetMapping("/{roomId}/members")
    public BaseResponse<List<ChatRoomMemberResponse>> getChatRoomMembers(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuth userAuth,
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
    @Operation(
            summary = "읽음 상태 조회",
            description = "채팅방의 읽음 상태 정보를 조회합니다."
    )
    @GetMapping("/{roomId}/read-status")
    public BaseResponse<ReadStatusRestResponse> getReadStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long roomId
    ) {
        Long userId = userAuth.getUserId();
        ReadStatusRestResponse status =
                chatRoomService.getReadStatus(groupId, roomId, userId);
        return new BaseResponse<>(status);
    }
}
