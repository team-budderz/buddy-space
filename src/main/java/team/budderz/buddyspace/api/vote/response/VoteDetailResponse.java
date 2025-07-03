package team.budderz.buddyspace.api.vote.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "투표 상세 응답 DTO")
public record VoteDetailResponse(
        @Schema(description = "투표 식별자", example = "1")
        Long voteId,

        @Schema(description = "투표 제목", example = "오늘 저녁 메뉴")
        String title,

        @Schema(description = "투표 종료 여부", example = "false")
        boolean isClosed,

        @Schema(description = "익명 투표 여부", example = "false")
        boolean isAnonymous,

        @Schema(description = "투표 옵션 목록")
        List<OptionDetailResponse> options,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "작성자 프로필 이미지 url", example = "https://profile.image")
        String authorImageUrl,

        @Schema(description = "투표 생성 일자", example = "2025-05-15")
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
                vote.getAuthor().getName(),
                authorImageUrl,
                vote.getCreatedAt().toLocalDate()
        );
    }

    @Schema(description = "투표 옵션 상세 응답 DTO")
    public record OptionDetailResponse(
            @Schema(description = "투표 옵션 식별자", example = "1") Long voteOptionId,
            @Schema(description = "투표 옵션 이름", example = "햄버거") String voteOptionName,
            @Schema(description = "투표 수", example = "3") int voteCount,
            @Schema(description = "투표자 이름 목록", example = "[\"김철수\", \"김영희\"]") List<String> voterName
    ) {
    }
}
