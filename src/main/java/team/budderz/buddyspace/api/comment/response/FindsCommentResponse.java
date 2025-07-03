package team.budderz.buddyspace.api.comment.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 목록 조회 응답 DTO")
public record FindsCommentResponse(
        @Schema(description = "댓글 식별자", example = "5")
        Long commentId,

        @Schema(description = "작성자 식별자", example = "3")
        Long userId,

        @Schema(description = "작성자 프로필 이미지 url", example = "https://profile.image")
        String userImgUrl,

        @Schema(description = "작성자 이름", example = "홍길동")
        String userName,

        @Schema(description = "댓글 내용", example = "댓글 내용 예시입니다.")
        String content,

        @Schema(description = "생성 일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime createdAt,

        @Schema(description = "대댓글 수", example = "3")
        Long commentNum

) {
    public static FindsCommentResponse of(
            Long commentId,
            Long userId,
            String userImgUrl,
            String userName,
            String content,
            LocalDateTime createdAt,
            Long commentNum
    ) {
        return new FindsCommentResponse(commentId, userId, userImgUrl, userName, content, createdAt, commentNum);
    }
}

