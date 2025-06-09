package team.budderz.buddyspace.api.vote.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitVoteRequest(
	@NotNull(message = "투표 옵션은 필수입니다")
	@Size(min = 2, message = "투표 옵션은 최소 2개 이상이어야 합니다")
	List<Long> voteOptionIds
) {
}
