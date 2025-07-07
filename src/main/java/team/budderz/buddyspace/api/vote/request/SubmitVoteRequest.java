package team.budderz.buddyspace.api.vote.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "투표 요청 DTO")
public record SubmitVoteRequest(
	@NotNull(message = "투표 옵션 선택은 필수입니다")
	@Schema(description = "투표 옵션 식별자", example = "1")
	List<Long> voteOptionIds
) {
}
