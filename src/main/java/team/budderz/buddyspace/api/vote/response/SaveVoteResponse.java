package team.budderz.buddyspace.api.vote.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;
import team.budderz.buddyspace.infra.database.vote.entity.VoteOption;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "투표 생성 응답 DTO")
public record SaveVoteResponse(
        @Schema(description = "투표 식별자", example = "1")
        Long voteId,

        @Schema(description = "투표 제목", example = "오늘 저녁 메뉴")
        String title,

        @Schema(description = "익명 투표 여부", example = "false")
        boolean isAnonymous,

        @Schema(description = "투표 옵션", example = "[\"햄버거\", \"떡볶이\"]")
        List<String> options,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "작성자 프로필 이미지 url", example = "https://profile.image")
        String authorImageUrl,

        @Schema(description = "투표 생성 일자", example = "2025-05-15")
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
