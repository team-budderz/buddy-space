package team.budderz.buddyspace.api.membership.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.membership.request.MemberRoleRequest;
import team.budderz.buddyspace.api.membership.response.MembershipResponse;
import team.budderz.buddyspace.domain.membership.service.MembershipService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/groups/{groupId}/members")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping("/requests")
    public BaseResponse<MembershipResponse> requestJoin(@PathVariable Long groupId,
                                                        @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.requestJoin(loginUserId, groupId);

        return new BaseResponse<>(response);
    }

    @PatchMapping("/{memberId}/approve")
    public BaseResponse<MembershipResponse> approveMember(@PathVariable Long groupId,
                                                          @PathVariable Long memberId,
                                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.approveMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(response);
    }

    @DeleteMapping("/{memberId}/reject")
    public BaseResponse<Void> rejectMember(@PathVariable Long groupId,
                                           @PathVariable Long memberId,
                                           @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        membershipService.rejectMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(null);
    }

    @DeleteMapping("/{memberId}/expel")
    public BaseResponse<Void> expelMember(@PathVariable Long groupId,
                                          @PathVariable Long memberId,
                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        membershipService.expelMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(null);
    }

    @PatchMapping("/{memberId}/block")
    public BaseResponse<MembershipResponse> blockMember(@PathVariable Long groupId,
                                                        @PathVariable Long memberId,
                                                        @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.blockMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(response);
    }

    @DeleteMapping("/{memberId}/unblock")
    public BaseResponse<Void> unblockMember(@PathVariable Long groupId,
                                            @PathVariable Long memberId,
                                            @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        membershipService.unblockMember(loginUserId, groupId, memberId);

        return new BaseResponse<>(null);
    }

    @PatchMapping("/{memberId}/role")
    public BaseResponse<MembershipResponse> updateMemberRole(@PathVariable Long groupId,
                                                             @PathVariable Long memberId,
                                                             @RequestBody @Valid MemberRoleRequest request,
                                                             @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse response = membershipService.updateMemberRole(loginUserId, groupId, memberId, request.role());

        return new BaseResponse<>(response);
    }

    @PatchMapping("/{memberId}/delegate")
    public BaseResponse<MembershipResponse> delegateLeader(@PathVariable Long groupId,
                                                           @PathVariable Long memberId,
                                                           @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        MembershipResponse responses = membershipService.delegateLeader(loginUserId, groupId, memberId);

        return new BaseResponse<>(responses);
    }

    @GetMapping
    public BaseResponse<MembershipResponse> findApprovedMembers(@PathVariable Long groupId,
                                                                @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserid = userAuth.getUserId();
        MembershipResponse response = membershipService.findApprovedMembers(loginUserid, groupId);

        return new BaseResponse<>(response);
    }

    @GetMapping("/requested")
    public BaseResponse<MembershipResponse> findRequestedMembers(@PathVariable Long groupId,
                                                                @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserid = userAuth.getUserId();
        MembershipResponse response = membershipService.findRequestedMembers(loginUserid, groupId);

        return new BaseResponse<>(response);
    }

    @GetMapping("/blocked")
    public BaseResponse<MembershipResponse> findBlockedMembers(@PathVariable Long groupId,
                                                                @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserid = userAuth.getUserId();
        MembershipResponse response = membershipService.findBlockedMembers(loginUserid, groupId);

        return new BaseResponse<>(response);
    }
}
