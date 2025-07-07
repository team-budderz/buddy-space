package team.budderz.buddyspace.api.post.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.post.entity.Post;

import java.time.LocalDateTime;

@Schema(description = "게시글 생성 응답 DTO")
public record SavePostResponse(
        @Schema(description = "게시글 식별자", example = "13")
        Long postId,

        @Schema(description = "모임 식별자", example = "25")
        Long groupId,

        @Schema(description = "작성자 식별자", example = "8")
        Long userId,

        @Schema(description = "게시글 내용", example = "게시글 내용 예시입니다.")
        String content,

        @Schema(description = "게시글 예약 일시", example = "null")
        LocalDateTime reserveAt,

        @Schema(description = "공지글 여부", example = "false")
        Boolean isNotice,

        @Schema(description = "게시글 작성 일시", example = "2025-06-17T16:40:27.9899751")
        LocalDateTime createdAt,

        @Schema(description = "게시글 수정 일시", example = "2025-06-18T16:40:27.9899751")
        LocalDateTime modifiedAt

) {
    public static SavePostResponse from(Post post) {
        return new SavePostResponse(
                post.getId(),
                post.getGroup().getId(),
                post.getUser().getId(),
                post.getContent(),
                post.getReserveAt(),
                post.getIsNotice(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }
}
