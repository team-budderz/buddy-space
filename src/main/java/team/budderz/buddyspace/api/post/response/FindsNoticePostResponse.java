package team.budderz.buddyspace.api.post.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.post.entity.Post;

@Schema(description = "공지글 조회 응답 DTO")
public record FindsNoticePostResponse(
        @Schema(description = "게시글 식별자", example = "1")
        Long postId,

        @Schema(description = "게시글 내용 (첨부파일 렌더링)", example = "<div>오늘의 전달 사항입니다.</div><img src=\"https://image.url\" data-id=\"13\">")
        String content
) {
    public static FindsNoticePostResponse from(Post post) {
        return new FindsNoticePostResponse(
                post.getId(),
                post.getContent()
        );
    }
}
