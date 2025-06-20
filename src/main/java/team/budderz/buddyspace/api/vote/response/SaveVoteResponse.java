package team.budderz.buddyspace.api.vote.response;

import team.budderz.buddyspace.infra.database.vote.entity.Vote;
import team.budderz.buddyspace.infra.database.vote.entity.VoteOption;

import java.time.LocalDate;
import java.util.List;

public record SaveVoteResponse(
        Long voteId,
        String title,
        boolean isAnonymous,
        List<String> options,
        String authorName,
        String authorImageUrl,
        LocalDate createdAt
) {
    public static SaveVoteResponse from(Vote vote, String authorImageUrl) {
        return new SaveVoteResponse(
                vote.getId(),
                vote.getTitle(),
                vote.isAnonymous(),
                vote.getOptions().stream().map(VoteOption::getContent).toList(),
                vote.getAuthor().getName(),
                authorImageUrl,
                vote.getCreatedAt().toLocalDate()
        );
    }
}
