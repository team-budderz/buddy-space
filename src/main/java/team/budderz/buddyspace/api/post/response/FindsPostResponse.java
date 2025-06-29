package team.budderz.buddyspace.api.post.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

public record FindsPostResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long profileAttachmentId,
        Long id,
        String userImgUrl,
        String userName,
        LocalDateTime createdAt,
        String content,
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
