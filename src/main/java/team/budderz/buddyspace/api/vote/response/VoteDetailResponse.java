package team.budderz.buddyspace.api.vote.response;

import java.time.LocalDate;
import java.util.List;

import team.budderz.buddyspace.infra.database.vote.entity.Vote;

public record VoteDetailResponse(
	Long voteId,
	String title,
	boolean isClosed,
	boolean isAnonymous,
	List<OptionDetailResponse> options,
	String authorName,
	String authorImageUrl,
	LocalDate createdAt
) {
	public static VoteDetailResponse from(Vote vote, List<OptionDetailResponse> optionDetailResponse) {
		return new VoteDetailResponse(
			vote.getId(),
			vote.getTitle(),
			vote.isClosed(),
			vote.isAnonymous(),
			optionDetailResponse,
			vote.getAuthor().getName(),
			vote.getAuthor().getImageUrl(),
			vote.getCreatedAt().toLocalDate()
		);
	}

	public record OptionDetailResponse(
		Long voteOptionId,
		String voteOptionName,
		int voteCount,
		List<String> voterName
	) {}
}
