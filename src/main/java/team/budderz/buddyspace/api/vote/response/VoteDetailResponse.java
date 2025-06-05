package team.budderz.buddyspace.api.vote.response;

import java.time.LocalDate;
import java.util.List;

import team.budderz.buddyspace.infra.database.vote.entity.Vote;
import team.budderz.buddyspace.infra.database.vote.entity.VoteOption;

public record VoteDetailResponse(
	Long voteId,
	String title,
	boolean isClosed,
	boolean isAnonymous,
	List<String> options,
	String authorName,
	String authorImageUrl,
	LocalDate createdAt
) {
	public static VoteDetailResponse from(Vote vote) {
		return new VoteDetailResponse(
			vote.getId(),
			vote.getTitle(),
			vote.isClosed(),
			vote.isAnonymous(),
			vote.getOptions().stream().map(VoteOption::getContent).toList(),
			vote.getAuthor().getName(),
			vote.getAuthor().getImageUrl(),
			vote.getCreatedAt().toLocalDate()
		);
	}
}
