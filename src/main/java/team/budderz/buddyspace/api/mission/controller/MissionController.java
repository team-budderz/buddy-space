package team.budderz.buddyspace.api.mission.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.mission.request.SaveMissionRequest;
import team.budderz.buddyspace.api.mission.request.UpdateMissionRequest;
import team.budderz.buddyspace.api.mission.response.MissionDetailResponse;
import team.budderz.buddyspace.api.mission.response.MissionResponse;
import team.budderz.buddyspace.api.mission.response.SaveMissionResponse;
import team.budderz.buddyspace.api.mission.response.UpdateMissionResponse;
import team.budderz.buddyspace.domain.mission.service.MissionService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/missions")
public class MissionController {

    private final MissionService missionService;

    @PostMapping
    public BaseResponse<SaveMissionResponse> saveMission(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @Valid @RequestBody SaveMissionRequest request
    ) {
        return new BaseResponse<>(missionService.saveMission(userAuth.getUserId(), groupId, request));
    }

    @PatchMapping("/{missionId}")
    public BaseResponse<UpdateMissionResponse> updateMission(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long missionId,
            @Valid @RequestBody UpdateMissionRequest request
    ) {
        return new BaseResponse<>(missionService.updateMission(userAuth.getUserId(), groupId, missionId, request));
    }

    @DeleteMapping("/{missionId}")
    public BaseResponse<Void> deleteMission(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long missionId
    ) {
        missionService.deleteMission(userAuth.getUserId(), groupId, missionId);
        return new BaseResponse<>(null);
    }

    @GetMapping
    public BaseResponse<List<MissionResponse>> findMissions(
            @PathVariable Long groupId
    ) {
        return new BaseResponse<>(missionService.findMissions(groupId));
    }

    @GetMapping("/{missionId}")
    public BaseResponse<MissionDetailResponse> findMissionDetail(
            @PathVariable Long groupId,
            @PathVariable Long missionId
    ) {
        return new BaseResponse<>(missionService.findMissionDetail(groupId, missionId));
    }

}
