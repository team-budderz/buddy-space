package team.budderz.buddyspace.api.vote.response;

import team.budderz.buddyspace.infra.database.vote.entity.Vote;

import java.time.LocalDate;
import java.util.List;

public record VoteDetailResponse(
        Long voteId,
        String title,
        boolean isClosed,
        boolean isAnonymous,
        List<OptionDetailResponse> options,
        Long authorId,
        String authorName,
        String authorImageUrl,
        LocalDate createdAt
) {
    public static VoteDetailResponse from(
            Vote vote,
            List<OptionDetailResponse> optionDetailResponse,
            String authorImageUrl
    ) {
        return new VoteDetailResponse(
                vote.getId(),
                vote.getTitle(),
                vote.isClosed(),
                vote.isAnonymous(),
                optionDetailResponse,
                vote.getAuthor().getId(),
                vote.getAuthor().getName(),
                authorImageUrl,
                vote.getCreatedAt().toLocalDate()
        );
    }

    public record OptionDetailResponse(
            Long voteOptionId,
            String voteOptionName,
            int voteCount,
            List<String> voterName
    ) {
    }
}
