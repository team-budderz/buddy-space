package team.budderz.buddyspace.api.vote.response;

import java.time.LocalDate;

import team.budderz.buddyspace.infra.database.vote.entity.Vote;

public record VoteResponse (
	Long voteId,
	String title,
	boolean isClosed,
	String authorName,
	LocalDate createdAt
) {
	public static VoteResponse from(Vote vote) {
		return new VoteResponse(
			vote.getId(),
			vote.getTitle(),
			vote.isClosed(),
			vote.getAuthor().getName(),
			vote.getCreatedAt().toLocalDate()
		);
	}
}
