package team.budderz.buddyspace.api.post.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.api.comment.response.FindsCommentResponse;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시글 상세 조회 응답 DTO")
public record FindPostResponse(
        @Schema(description = "작성자 식별자", example = "1")
        Long userId,

        @Schema(description = "작성자 프로필 이미지 url", example = "https://profile.image")
        String userImgUrl,

        @Schema(description = "작성자 이름", example = "김벗터")
        String userName,

        @Schema(description = "게시글 작성 일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime createdAt,

        @Schema(description = "게시글 내용 (첨부파일 렌더링)", example = "<div>예쁜 하늘 사진 공유합니다.</div><img src=\"https://image.url\" data-id=\"13\">")
        String renderedContent,

        @Schema(description = "공지글 여부", example = "false")
        Boolean isNotice,

        @Schema(description = "댓글 수", example = "6")
        Long commentNum,

        @Schema(description = "댓글 목록")
        List<FindsCommentResponse> comments

) {
    public static FindPostResponse of(
            Long userId,
            String userImgUrl,
            String userName,
            LocalDateTime createdAt,
            String renderedContent,
            Boolean isNotice,
            Long commentNum,
            List<FindsCommentResponse> comments
    ) {
        return new FindPostResponse(userId, userImgUrl, userName, createdAt, renderedContent, isNotice, commentNum, comments);
    }
}

