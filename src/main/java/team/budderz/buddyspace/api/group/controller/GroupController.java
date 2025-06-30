package team.budderz.buddyspace.api.group.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.group.request.SaveGroupRequest;
import team.budderz.buddyspace.api.group.request.UpdateGroupRequest;
import team.budderz.buddyspace.api.group.response.GroupListResponse;
import team.budderz.buddyspace.api.group.response.GroupResponse;
import team.budderz.buddyspace.domain.group.service.GroupService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.response.PageResponse;
import team.budderz.buddyspace.global.security.UserAuth;
import team.budderz.buddyspace.infra.database.group.entity.GroupSortType;

import java.util.List;

/**
 * 모임 기본 CRUD
 */
@Validated
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<GroupResponse> saveGroup(
            @RequestPart("request") @Valid SaveGroupRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.saveGroup(loginUserId, request, coverImage);

        return new BaseResponse<>(response);
    }

    @PutMapping(value = "/{groupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<GroupResponse> updateGroup(
            @PathVariable Long groupId,
            @RequestPart("request") @Valid UpdateGroupRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.updateGroup(loginUserId, groupId, request, coverImage);

        return new BaseResponse<>(response);
    }

    @GetMapping("/{groupId}")
    public BaseResponse<GroupResponse> findGroup(@PathVariable Long groupId,
                                                 @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.findGroup(groupId, loginUserId);

        return new BaseResponse<>(response);
    }

    @PatchMapping("/{groupId}/address")
    public BaseResponse<GroupResponse> updateGroupAddress(@PathVariable Long groupId,
                                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.updateGroupAddress(groupId, loginUserId);

        return new BaseResponse<>(response);
    }

    @PatchMapping("/{groupId}/neighborhood-auth-required")
    public BaseResponse<GroupResponse> updateGroupNeighborhoodAuthRequired(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestBody @NotNull Boolean neighborhoodAuthRequired
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response =
                groupService.updateGroupNeighborhoodAuthRequired(groupId, loginUserId, neighborhoodAuthRequired);

        return new BaseResponse<>(response);
    }

    @DeleteMapping("/{groupId}")
    public BaseResponse<Void> deleteGroup(@PathVariable Long groupId,
                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        groupService.deleteGroup(loginUserId, groupId);

        return new BaseResponse<>(null);
    }

    @GetMapping("/my")
    public BaseResponse<PageResponse<GroupListResponse>> findGroupsByUser(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam(defaultValue = "0") int page

    ) {
        Long loginUserId = userAuth.getUserId();
        PageResponse<GroupListResponse> responses = groupService.findGroupsByUser(loginUserId, page);

        return new BaseResponse<>(responses);
    }

    @GetMapping("/my-requested")
    public BaseResponse<List<GroupResponse>> findMyRequested(@AuthenticationPrincipal UserAuth userAuth) {

        Long loginUserId = userAuth.getUserId();
        List<GroupResponse> responses = groupService.findMyRequested(loginUserId);

        return new BaseResponse<>(responses);
    }

    @GetMapping("/on")
    public BaseResponse<PageResponse<GroupListResponse>> findOnlineGroups(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam String sort,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupSortType sortType = GroupSortType.from(sort);
        PageResponse<GroupListResponse> responses = groupService.findOnlineGroups(loginUserId, sortType, interest, page);

        return new BaseResponse<>(responses);
    }

    @GetMapping("/off")
    public BaseResponse<PageResponse<GroupListResponse>> findOfflineGroups(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam String sort,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupSortType sortType = GroupSortType.from(sort);
        PageResponse<GroupListResponse> responses = groupService.findOfflineGroups(loginUserId, sortType, interest, page);

        return new BaseResponse<>(responses);
    }

    @GetMapping("/search")
    public BaseResponse<PageResponse<GroupListResponse>> searchGroupsByName(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam String keyword,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long loginUserId = userAuth.getUserId();
        PageResponse<GroupListResponse> responses = groupService.searchGroupsByName(loginUserId, keyword, interest, page);

        return new BaseResponse<>(responses);
    }
}
