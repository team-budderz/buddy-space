package team.budderz.buddyspace.api.vote.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record SubmitVoteRequest(
	@NotNull
	List<Long> voteOptionIds
) {
}
