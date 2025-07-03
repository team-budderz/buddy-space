package team.budderz.buddyspace.api.mission.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "미션 관리", description = "미션 관련 API")
public class MissionController {

    private final MissionService missionService;

    @Operation(
            summary = "미션 생성",
            description = "새로운 미션을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "미션 생성 성공")
            }
    )
    @PostMapping
    public BaseResponse<SaveMissionResponse> saveMission(@AuthenticationPrincipal UserAuth userAuth,
                                                         @PathVariable Long groupId,
                                                         @Valid @RequestBody SaveMissionRequest request) {
        SaveMissionResponse response = missionService.saveMission(userAuth.getUserId(), groupId, request);
        return new BaseResponse<>(response);
    }

    @Operation(
            summary = "미션 정보 수정",
            description = "기존 미션의 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "미션 정보 수정 성공")
            }
    )
    @PatchMapping("/{missionId}")
    public BaseResponse<UpdateMissionResponse> updateMission(@AuthenticationPrincipal UserAuth userAuth,
                                                             @PathVariable Long groupId,
                                                             @PathVariable Long missionId,
                                                             @Valid @RequestBody UpdateMissionRequest request) {
        UpdateMissionResponse response =
                missionService.updateMission(userAuth.getUserId(), groupId, missionId, request);
        return new BaseResponse<>(response);
    }

    @Operation(
            summary = "미션 삭제",
            description = "특정 미션을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "미션 삭제 성공")
            }
    )
    @DeleteMapping("/{missionId}")
    public BaseResponse<Void> deleteMission(@AuthenticationPrincipal UserAuth userAuth,
                                            @PathVariable Long groupId,
                                            @PathVariable Long missionId) {
        missionService.deleteMission(userAuth.getUserId(), groupId, missionId);
        return new BaseResponse<>(null);
    }

    @Operation(
            summary = "미션 목록 조회",
            description = "모임의 미션 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "미션 목록 조회 성공")
            }
    )
    @GetMapping
    public BaseResponse<List<MissionResponse>> findMissions(@PathVariable Long groupId) {
        List<MissionResponse> responses = missionService.findMissions(groupId);
        return new BaseResponse<>(responses);
    }

    @Operation(
            summary = "미션 상세 조회",
            description = "미션의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "미션 상세 조회 성공")
            }
    )
    @GetMapping("/{missionId}")
    public BaseResponse<MissionDetailResponse> findMissionDetail(@PathVariable Long groupId,
                                                                 @PathVariable Long missionId) {
        MissionDetailResponse response = missionService.findMissionDetail(groupId, missionId);
        return new BaseResponse<>(response);
    }
}
