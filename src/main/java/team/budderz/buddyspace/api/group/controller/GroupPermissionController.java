package team.budderz.buddyspace.api.group.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.group.request.GroupPermissionRequest;
import team.budderz.buddyspace.api.group.response.GroupPermissionResponse;
import team.budderz.buddyspace.domain.group.service.GroupPermissionService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

/**
 * 모임 기능별 권한 설정
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/permissions")
public class GroupPermissionController {

    private final GroupPermissionService permissionService;

    @PostMapping
    public BaseResponse<GroupPermissionResponse> updateGroupPermission(
            @PathVariable Long groupId,
            @RequestBody @Valid List<GroupPermissionRequest> requests,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupPermissionResponse response = permissionService.updateGroupPermission(loginUserId, groupId, requests);

        return new BaseResponse<>(response);
    }

    @GetMapping
    public BaseResponse<GroupPermissionResponse> findGroupPermissions(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupPermissionResponse response = permissionService.findGroupPermissions(loginUserId, groupId);

        return new BaseResponse<>(response);
    }
}
