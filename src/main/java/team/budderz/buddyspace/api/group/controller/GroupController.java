package team.budderz.buddyspace.api.group.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.budderz.buddyspace.api.group.request.SaveGroupRequest;
import team.budderz.buddyspace.api.group.response.SaveGroupResponse;
import team.budderz.buddyspace.domain.group.service.GroupService;
import team.budderz.buddyspace.global.response.BaseResponse;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public BaseResponse<SaveGroupResponse> saveGroup(@RequestBody SaveGroupRequest request) {
        Long userId = 1L; // TODO
        SaveGroupResponse response = groupService.saveGroup(
                userId,
                request.name(),
                request.access(),
                request.type(),
                request.interest()
        );

        return new BaseResponse<>(response);
    }
}
