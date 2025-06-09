package team.budderz.buddyspace.api.vote.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitVoteRequest(
	@NotNull(message = "투표 옵션 선택은 필수입니다")
	List<Long> voteOptionIds
) {
}
