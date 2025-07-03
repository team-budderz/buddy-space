package team.budderz.buddyspace.api.vote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.vote.request.SaveVoteRequest;
import team.budderz.buddyspace.api.vote.request.SubmitVoteRequest;
import team.budderz.buddyspace.api.vote.response.SaveVoteResponse;
import team.budderz.buddyspace.api.vote.response.VoteDetailResponse;
import team.budderz.buddyspace.api.vote.response.VoteResponse;
import team.budderz.buddyspace.domain.vote.service.VoteService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "투표 관리", description = "투표 관련 API")
public class VoteController {
    private final VoteService voteService;

    @Operation(
            summary = "투표 생성",
            description = "새로운 투표를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "투표 생성 성공")
            }
    )
    @PostMapping("/groups/{groupId}/votes")
    public BaseResponse<SaveVoteResponse> saveVote(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @Valid @RequestBody SaveVoteRequest request
    ) {
        return new BaseResponse<>(voteService.saveVote(userAuth.getUserId(), groupId, request));
    }

    @Operation(
            summary = "투표 수정",
            description = "투표 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "투표 수정 성공")
            }
    )
    @PutMapping("/groups/{groupId}/votes/{voteId}")
    public BaseResponse<SaveVoteResponse> updateVote(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long voteId,
            @Valid @RequestBody SaveVoteRequest request
    ) {
        SaveVoteResponse saveVoteResponse = voteService.updateVote(userAuth.getUserId(), groupId, voteId, request);
        return new BaseResponse<>(saveVoteResponse);
    }

    @Operation(
            summary = "투표 삭제",
            description = "투표를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "투표 삭제 성공")
            }
    )
    @DeleteMapping("/groups/{groupId}/votes/{voteId}")
    public BaseResponse<Void> deleteVote(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long voteId
    ) {
        voteService.deleteVote(userAuth.getUserId(), groupId, voteId);
        return new BaseResponse<>(null);
    }

    @Operation(
            summary = "투표 목록 조회",
            description = "투표 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "투표 목록 조회 성공")
            }
    )
    @GetMapping("/groups/{groupId}/votes")
    public BaseResponse<List<VoteResponse>> findVote(
            @PathVariable Long groupId
    ) {
        return new BaseResponse<>(voteService.findVote(groupId));
    }

    @Operation(
            summary = "투표 상세 조회",
            description = "투표 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "투표 상세 조회 성공")
            }
    )
    @GetMapping("/groups/{groupId}/votes/{voteId}")
    public BaseResponse<VoteDetailResponse> findVote(
            @PathVariable Long groupId,
            @PathVariable Long voteId
    ) {
        return new BaseResponse<>(voteService.findVote(groupId, voteId));
    }

    @Operation(
            summary = "투표 참여",
            description = "투표에 참여합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "투표 참여 성공")
            }
    )
    @PostMapping("/groups/{groupId}/votes/{voteId}/submit")
    public BaseResponse<Void> submitVote(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long voteId,
            @Valid @RequestBody SubmitVoteRequest request
    ) {
        voteService.submitVote(userAuth.getUserId(), groupId, voteId, request);
        return new BaseResponse<>(null);
    }

    @Operation(
            summary = "투표 종료",
            description = "투표를 종료합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "투표 종료 성공")
            }
    )
    @PostMapping("/groups/{groupId}/votes/{voteId}/close")
    public BaseResponse<Void> closeVote(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long voteId
    ) {
        voteService.closeVote(userAuth.getUserId(), groupId, voteId);
        return new BaseResponse<>(null);
    }
}
