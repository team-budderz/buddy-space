package team.budderz.buddyspace.api.vote.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;

import java.time.LocalDate;

@Schema(description = "투표 조회 응답 DTO")
public record VoteResponse (
        @Schema(description = "투표 식별자", example = "1")
        Long voteId,

        @Schema(description = "투표 제목", example = "오늘 저녁 메뉴")
        String title,

        @Schema(description = "투표 종료 여부", example = "false")
        boolean isClosed,

        @Schema(description = "작성자 식별자", example = "1")
        Long authorId,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "투표 생성 일자", example = "2025-05-15")
        LocalDate createdAt

) {
	public static VoteResponse from(Vote vote) {
		return new VoteResponse(
			vote.getId(),
			vote.getTitle(),
			vote.isClosed(),
			vote.getAuthor().getId(),
			vote.getAuthor().getName(),
			vote.getCreatedAt().toLocalDate()
		);
	}
}
