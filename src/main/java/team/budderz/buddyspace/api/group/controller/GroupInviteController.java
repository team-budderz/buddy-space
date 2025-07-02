package team.budderz.buddyspace.api.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.group.response.GroupInviteResponse;
import team.budderz.buddyspace.domain.group.service.GroupInviteService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/invites")
@Tag(name = "모임 초대 링크 관리", description = "모임 초대 링크 관련 API")
public class GroupInviteController {

    private final GroupInviteService groupInviteService;

    @Operation(summary = "모임 초대 링크 생성", description = "모임의 초대 링크를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "모임 초대 링크 생성 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping
    public BaseResponse<GroupInviteResponse> updateInviteLink(@PathVariable Long groupId,
                                                              @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupInviteResponse response = groupInviteService.updateInviteLink(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 초대 링크 조회", description = "모임의 초대 링크를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "모임 초대 링크 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping
    public BaseResponse<GroupInviteResponse> findInviteLink(@PathVariable Long groupId,
                                                            @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupInviteResponse response = groupInviteService.findInviteLink(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "초대 링크 삭제", description = "모임의 초대 링크를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "모임 초대 링크 삭제 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping
    public BaseResponse<GroupInviteResponse> deleteInviteLink(@PathVariable Long groupId,
                                                              @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupInviteResponse response = groupInviteService.deleteInviteLink(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

}
