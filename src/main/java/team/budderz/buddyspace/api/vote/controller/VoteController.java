package team.budderz.buddyspace.api.vote.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.vote.request.SaveVoteRequest;
import team.budderz.buddyspace.api.vote.request.SubmitVoteRequest;
import team.budderz.buddyspace.api.vote.response.SaveVoteResponse;
import team.budderz.buddyspace.api.vote.response.VoteDetailResponse;
import team.budderz.buddyspace.api.vote.response.VoteResponse;
import team.budderz.buddyspace.domain.vote.service.VoteService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class VoteController {
	private final VoteService voteService;

	@PostMapping("/groups/{groupId}/votes")
	public BaseResponse<SaveVoteResponse> saveVote(
		@AuthenticationPrincipal UserAuth userAuth,
		@PathVariable Long groupId,
		@Valid @RequestBody SaveVoteRequest request
	) {
		return new BaseResponse<>(voteService.saveVote(userAuth.getUserId(), groupId, request));
	}

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

	@DeleteMapping("/groups/{groupId}/votes/{voteId}")
	public BaseResponse<Void> deleteVote(
		@AuthenticationPrincipal UserAuth userAuth,
		@PathVariable Long groupId,
		@PathVariable Long voteId
	) {
		voteService.deleteVote(userAuth.getUserId(), groupId, voteId);
		return new BaseResponse<>(null);
	}

	@GetMapping("/groups/{groupId}/votes")
	public BaseResponse<List<VoteResponse>> findVote(
		@PathVariable Long groupId
	) {
		return new BaseResponse<>(voteService.findVote(groupId));
	}

	@GetMapping("/groups/{groupId}/votes/{voteId}")
	public BaseResponse<VoteDetailResponse> findVote(
		@PathVariable Long groupId,
		@PathVariable Long voteId
	) {
		return new BaseResponse<>(voteService.findVote(groupId, voteId));
	}

	@PostMapping("/groups/{groupId}/votes/{voteId}/submit")
	public BaseResponse<Void> submitVote(
		@AuthenticationPrincipal UserAuth userAuth,
		@PathVariable Long groupId,
		@PathVariable Long voteId,
		@Valid @RequestBody SubmitVoteRequest request
	) {
		voteService.sumbitVote(userAuth.getUserId(), groupId, voteId, request);
		return new BaseResponse<>(null);
	}
}
