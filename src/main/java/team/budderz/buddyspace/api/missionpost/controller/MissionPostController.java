package team.budderz.buddyspace.api.missionpost.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.missionpost.request.MissionPostRequest;
import team.budderz.buddyspace.api.missionpost.response.MissionPostDetailResponse;
import team.budderz.buddyspace.api.missionpost.response.MissionPostResponse;
import team.budderz.buddyspace.domain.missionpost.service.MissionPostService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/missions/{missionId}/posts")
public class MissionPostController {

    private final MissionPostService missionPostService;

    @PostMapping
    public BaseResponse<Void> saveMissionPost(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionPostRequest request
    ) {
        missionPostService.saveMissionPost(userAuth.getUserId(), groupId, missionId, request);
        return new BaseResponse<>(null);
    }

    @PatchMapping("/{postId}")
    public BaseResponse<Void> updateMissionPost(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long missionId,
            @PathVariable Long postId,
            @Valid @RequestBody MissionPostRequest request
    ) {
        missionPostService.updateMissionPost(userAuth.getUserId(), groupId, missionId, postId, request);
        return new BaseResponse<>(null);
    }

    @DeleteMapping("/{postId}")
    public BaseResponse<Void> deleteMissionPost(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long missionId,
            @PathVariable Long postId
    ) {
        missionPostService.deleteMissionPost(userAuth.getUserId(), groupId, missionId, postId);
        return new BaseResponse<>(null);
    }

    @GetMapping
    public BaseResponse<List<MissionPostResponse>> findMissionPosts(
            @PathVariable Long groupId,
            @PathVariable Long missionId
    ) {
        return new BaseResponse<>(missionPostService.findMissionPosts(groupId, missionId));
    }

    @GetMapping("/{postId}")
    public BaseResponse<MissionPostDetailResponse> findMissionPostDetail(
            @PathVariable Long groupId,
            @PathVariable Long missionId,
            @PathVariable Long postId
    ) {
        return new BaseResponse<>(missionPostService.findMissionPostDetail(groupId, missionId, postId));
    }
}
