package team.budderz.buddyspace.api.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "모임 기능별 권한 관리", description = "모임 기능별 권한 관련 API")
public class GroupPermissionController {

    private final GroupPermissionService permissionService;

    @Operation(
            summary = "모임 기능별 권한 설정",
            description = "모임의 기능별 접근 권한을 설정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "모임 기능별 접근 권한 설정 성공")
            }
    )
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

    @Operation(
            summary = "모임 기능별 권한 조회",
            description = "모임의 기능별 접근 권한을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "모임 기능별 접근 권한 조회 성공")
            }
    )
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
