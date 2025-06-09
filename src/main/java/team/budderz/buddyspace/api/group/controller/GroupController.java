package team.budderz.buddyspace.api.group.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.group.request.SaveGroupRequest;
import team.budderz.buddyspace.api.group.request.UpdateGroupRequest;
import team.budderz.buddyspace.api.group.response.GroupListResponse;
import team.budderz.buddyspace.api.group.response.GroupResponse;
import team.budderz.buddyspace.domain.group.service.GroupService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.response.PageResponse;
import team.budderz.buddyspace.global.security.UserAuth;
import team.budderz.buddyspace.infra.database.group.entity.GroupSortType;

/**
 * 모임 기본 CRUD
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public BaseResponse<GroupResponse> saveGroup(@RequestBody @Valid SaveGroupRequest request,
                                                 @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.saveGroup(
                loginUserId,
                request.name(),
                request.coverImageUrl(),
                request.access(),
                request.type(),
                request.interest()
        );

        return new BaseResponse<>(response);
    }

    @PutMapping("/{groupId}")
    public BaseResponse<GroupResponse> updateGroup(@RequestBody @Valid UpdateGroupRequest request,
                                                   @PathVariable Long groupId,
                                                   @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.updateGroup(
                loginUserId,
                groupId,
                request.name(),
                request.description(),
                request.coverImageUrl(),
                request.access(),
                request.type(),
                request.interest()
        );

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

    @GetMapping("/on")
    public BaseResponse<PageResponse<GroupListResponse>> findOnlineGroups(
            @RequestParam String sort,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") int page
    ) {
        GroupSortType sortType = GroupSortType.from(sort);
        PageResponse<GroupListResponse> responses = groupService.findOnlineGroups(sortType, interest, page);

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
            @RequestParam String keyword,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") int page
    ) {
        PageResponse<GroupListResponse> responses = groupService.searchGroupsByName(keyword, interest, page);

        return new BaseResponse<>(responses);
    }
}
