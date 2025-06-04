package team.budderz.buddyspace.api.vote.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.vote.request.SaveVoteRequest;
import team.budderz.buddyspace.api.vote.response.SaveVoteResponse;
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
}
