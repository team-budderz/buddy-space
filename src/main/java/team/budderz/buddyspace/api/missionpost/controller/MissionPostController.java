package team.budderz.buddyspace.api.missionpost.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "미션 인증 관리", description = "미션 인증 관련 API")
public class MissionPostController {

    private final MissionPostService missionPostService;

    @Operation(summary = "미션 인증 글 생성", description = "새로운 미션 인증 글을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "미션 인증 글 생성 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping
    public BaseResponse<Void> saveMissionPost(@AuthenticationPrincipal UserAuth userAuth,
                                              @PathVariable Long groupId,
                                              @PathVariable Long missionId,
                                              @Valid @RequestBody MissionPostRequest request) {
        missionPostService.saveMissionPost(userAuth.getUserId(), groupId, missionId, request);
        return new BaseResponse<>(null);
    }

    @Operation(summary = "미션 인증 글 수정", description = "기존의 미션 인증 글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "미션 인증 글 수정 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/{postId}")
    public BaseResponse<Void> updateMissionPost(@AuthenticationPrincipal UserAuth userAuth,
                                                @PathVariable Long groupId,
                                                @PathVariable Long missionId,
                                                @PathVariable Long postId,
                                                @Valid @RequestBody MissionPostRequest request) {
        missionPostService.updateMissionPost(userAuth.getUserId(), groupId, missionId, postId, request);
        return new BaseResponse<>(null);
    }

    @Operation(summary = "미션 인증 글 삭제", description = "특정 미션 인증 글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "미션 인증 글 삭제 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/{postId}")
    public BaseResponse<Void> deleteMissionPost(@AuthenticationPrincipal UserAuth userAuth,
                                                @PathVariable Long groupId,
                                                @PathVariable Long missionId,
                                                @PathVariable Long postId) {
        missionPostService.deleteMissionPost(userAuth.getUserId(), groupId, missionId, postId);
        return new BaseResponse<>(null);
    }

    @Operation(summary = "특정 미션의 인증 글 목록 조회", description = "특정 미션의 인증 글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "특정 미션의 인증 글 목록 조회",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping
    public BaseResponse<List<MissionPostResponse>> findMissionPosts(@PathVariable Long groupId,
                                                                    @PathVariable Long missionId) {
        List<MissionPostResponse> responses = missionPostService.findMissionPosts(groupId, missionId);
        return new BaseResponse<>(responses);
    }

    @Operation(summary = "미션 인증 글 상세 조회", description = "특정 미션 인증 글의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "미션 인증 글 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/{postId}")
    public BaseResponse<MissionPostDetailResponse> findMissionPostDetail(@PathVariable Long groupId,
                                                                         @PathVariable Long missionId,
                                                                         @PathVariable Long postId) {
        MissionPostDetailResponse response = missionPostService.findMissionPostDetail(groupId, missionId, postId);
        return new BaseResponse<>(response);
    }
}
