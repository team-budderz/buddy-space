package team.budderz.buddyspace.api.post.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "게시글 목록 조회 응답 DTO")
public record FindsPostResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "작성자 프로필 이미지 식별자 (응답에 미포함)", example = "null")
        Long profileAttachmentId,

        @Schema(description = "게시글 식별자", example = "1")
        Long id,

        @Schema(description = "작성자 프로필 이미지 url", example = "https://profile.image")
        String userImgUrl,

        @Schema(description = "작성자 이름", example = "김벗터")
        String userName,

        @Schema(description = "게시글 작성일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime createdAt,

        @Schema(description = "게시글 내용 (첨부파일 렌더링)", example = "<div>예쁜 하늘 사진 공유합니다.</div><img src=\"https://image.url\" data-id=\"13\">")
        String content,

        @Schema(description = "댓글 수", example = "9")
        Long commentsNum

) {
    public FindsPostResponse withProfileImageUrl(String url) {
        return new FindsPostResponse(
                null,
                this.id,
                url,
                this.userName,
                this.createdAt,
                this.content,
                this.commentsNum
        );
    }

    public FindsPostResponse withRenderedContent(String renderedContent) {
        return new FindsPostResponse(
                this.profileAttachmentId,
                this.id,
                this.userImgUrl,
                this.userName,
                this.createdAt,
                renderedContent,
                this.commentsNum
        );
    }
}
