package team.budderz.buddyspace.api.comment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;

import java.time.LocalDateTime;

@Schema(description = "대댓글 목록 조회 응답 DTO")
public record FindsRecommentResponse(
        @Schema(description = "작성자 식별자", example = "5")
        Long userId,

        @Schema(description = "작성자 프로필 이미지 url", example = "https://profile.image")
        String userImgUrl,

        @Schema(description = "작성자 식별자", example = "5")
        String userName,

        @Schema(description = "대댓글의 부모 댓글 식별자", example = "9")
        Long commentId,

        @Schema(description = "대댓글 식별자", example = "12")
        Long recommentId,

        @Schema(description = "댓글 내용", example = "댓글 내용 예시입니다.")
        String content,

        @Schema(description = "생성 일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime createdAt

) {
    public static FindsRecommentResponse from(Long commentId, Comment recomment, String userImgUrl) {
        return new FindsRecommentResponse(
                recomment.getUser().getId(),
                userImgUrl,
                recomment.getUser().getName(),
                commentId,
                recomment.getId(),
                recomment.getContent(),
                recomment.getCreatedAt()
        );
    }
}
