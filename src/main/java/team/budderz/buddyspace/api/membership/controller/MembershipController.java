package team.budderz.buddyspace.api.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.membership.request.MemberRoleRequest;
import team.budderz.buddyspace.api.membership.response.MemberResponse;
import team.budderz.buddyspace.api.membership.response.MembershipResponse;
import team.budderz.buddyspace.domain.membership.service.MembershipService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
@Tag(name = "모임 멤버 관리", description = "모임 멤버 관련 API")
public class MembershipController {

    private final MembershipService membershipService;

    @Operation(summary = "특정 모임에 가입된 정보 조회",
            description = "로그인한 사용자가 가입된 특정 모임의 가입 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "특정 모임에 가입된 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/membership")
    public BaseResponse<MemberResponse> findMyMembership(@PathVariable Long groupId,
                                                         @AuthenticationPrincipal UserAuth userAuth) {

        Long loginUserId = userAuth.getUserId();
        MemberResponse response = membershipService.findMyMembership(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 가입 요청", description = "모임에 가입을 요청합니다.")
    @ApiResponse(responseCode = "200", description = "모임 가입 요청 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/members/requests")
    public BaseResponse<MembershipResponse> requestJoin(@PathVariable Long groupId,
                                                        @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.requestJoin(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 가입 요청 취소", description = "모임 가입 요청을 취소합니다.")
    @ApiResponse(responseCode = "200", description = "모임 가입 요청 취소 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/cancel-requests")
    public BaseResponse<MembershipResponse> cancelRequest(@PathVariable Long groupId,
                                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        membershipService.cancelRequest(loginUserId, groupId);

        return new BaseResponse<>(null);
    }

    @Operation(summary = "모임 가입 요청 승인", description = "모임 가입 요청을 승인합니다.")
    @ApiResponse(responseCode = "200", description = "모임 가입 요청 승인 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/members/{memberId}/approve")
    public BaseResponse<MembershipResponse> approveMember(@PathVariable Long groupId,
                                                          @PathVariable Long memberId,
                                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.approveMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 가입 요청 거절", description = "모임 가입 요청을 거절합니다.")
    @ApiResponse(responseCode = "200", description = "모임 가입 요청 거절 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/members/{memberId}/reject")
    public BaseResponse<Void> rejectMember(@PathVariable Long groupId,
                                           @PathVariable Long memberId,
                                           @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        membershipService.rejectMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(null);
    }

    @Operation(summary = "모임 멤버 강제 탈퇴", description = "모임의 특정 멤버를 강제 탈퇴합니다.")
    @ApiResponse(responseCode = "200", description = "모임 멤버 강제 탈퇴 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/members/{memberId}/expel")
    public BaseResponse<Void> expelMember(@PathVariable Long groupId,
                                          @PathVariable Long memberId,
                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        membershipService.expelMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(null);
    }

    @Operation(summary = "모임 멤버 차단", description = "모임의 특정 멤버를 차단합니다.")
    @ApiResponse(responseCode = "200", description = "모임 멤버 차단 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/members/{memberId}/block")
    public BaseResponse<MembershipResponse> blockMember(@PathVariable Long groupId,
                                                        @PathVariable Long memberId,
                                                        @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.blockMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 차단 멤버 차단 해제", description = "모임에서 차단된 멤버의 차단을 해제합니다.")
    @ApiResponse(responseCode = "200", description = "모임 차단 멤버 차단 해제 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/members/{memberId}/unblock")
    public BaseResponse<Void> unblockMember(@PathVariable Long groupId,
                                            @PathVariable Long memberId,
                                            @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        membershipService.unblockMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(null);
    }

    @Operation(summary = "모임 탈퇴", description = "가입된 모임에서 탈퇴합니다.")
    @ApiResponse(responseCode = "200", description = "모임 탈퇴 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/withdraw")
    public BaseResponse<Void> withdrawGroup(@PathVariable Long groupId,
                                            @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        membershipService.withdrawGroup(loginUserId, groupId);

        return new BaseResponse<>(null);
    }

    @Operation(summary = "모임 멤버 권한 설정", description = "모임 멤버의 권한을 설정합니다.")
    @ApiResponse(responseCode = "200", description = "모임 멤버 권한 설정 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/members/{memberId}/role")
    public BaseResponse<MembershipResponse> updateMemberRole(@PathVariable Long groupId,
                                                             @PathVariable Long memberId,
                                                             @RequestBody @Valid MemberRoleRequest request,
                                                             @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.updateMemberRole(loginUserId, groupId, memberId, request.role());

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 리더 위임", description = "모임의 리더를 위임합니다.")
    @ApiResponse(responseCode = "200", description = "모임 리더 위임 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/members/{memberId}/delegate")
    public BaseResponse<MembershipResponse> delegateLeader(@PathVariable Long groupId,
                                                           @PathVariable Long memberId,
                                                           @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse responses = membershipService.delegateLeader(loginUserId, groupId, memberId);

        return new BaseResponse<>(responses);
    }

    @Operation(summary = "모임에 가입된 멤버 목록 조회", description = "특정 모임에 가입 상태인 멤버 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "모임에 가입된 멤버 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/members")
    public BaseResponse<MembershipResponse> findApprovedMembers(@PathVariable Long groupId,
                                                                @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserid = userAuth.getUserId();
        MembershipResponse response = membershipService.findApprovedMembers(loginUserid, groupId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "가입 요청한 모임 목록 조회", description = "로그인한 사용자가 가입 요청한 모임 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "가입 요청한 모임 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/members/requested")
    public BaseResponse<MembershipResponse> findRequestedMembers(@PathVariable Long groupId,
                                                                 @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserid = userAuth.getUserId();
        MembershipResponse response = membershipService.findRequestedMembers(loginUserid, groupId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임에 차단된 멤버 목록 조회", description = "특정 모임에서 차단된 멤버 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "모임에 차단된 멤버 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/members/blocked")
    public BaseResponse<MembershipResponse> findBlockedMembers(@PathVariable Long groupId,
                                                               @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserid = userAuth.getUserId();
        MembershipResponse response = membershipService.findBlockedMembers(loginUserid, groupId);

        return new BaseResponse<>(response);
    }
}
