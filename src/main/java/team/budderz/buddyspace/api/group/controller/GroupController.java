package team.budderz.buddyspace.api.group.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.group.request.GroupRequest;
import team.budderz.buddyspace.api.group.response.GroupResponse;
import team.budderz.buddyspace.domain.group.service.GroupService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public BaseResponse<GroupResponse> saveGroup(@RequestBody @Valid GroupRequest request,
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
    public BaseResponse<GroupResponse> updateGroup(@RequestBody @Valid GroupRequest request,
                                                   @PathVariable Long groupId,
                                                   @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.updateGroup(
                loginUserId,
                groupId,
                request.name(),
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
}
