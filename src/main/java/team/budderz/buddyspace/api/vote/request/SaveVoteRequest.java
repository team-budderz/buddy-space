package team.budderz.buddyspace.api.vote.request;

import java.util.List;

public record SaveVoteRequest(
	String title,
	List<String> options,
	boolean isAnonymous
) {
}
