package team.budderz.buddyspace.api.comment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.comment.entity.Comment;

import java.time.LocalDateTime;

@Schema(description = "댓글 생성/수정 응답 DTO")
public record CommentResponse(
        @Schema(description = "모임 식별자", example = "3")
        Long groupId,

        @Schema(description = "작성자 식별자", example = "5")
        Long userId,

        @Schema(description = "게시글 식별자", example = "1")
        Long postId,

        @Schema(description = "댓글 내용", example = "댓글 내용 예시입니다.")
        String content,

        @Schema(description = "생성 일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime createdAt,

        @Schema(description = "수정 일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime modifiedAt

) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getPost().getGroup().getId(),
                comment.getUser().getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getModifiedAt()
        );
    }
}
