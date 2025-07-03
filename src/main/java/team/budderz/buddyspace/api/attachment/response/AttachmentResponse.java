package team.budderz.buddyspace.api.attachment.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;

import java.time.LocalDateTime;

@Schema(description = "첨부파일 응답 DTO")
public record AttachmentResponse(
        @Schema(description = "첨부파일 식별자", example = "1")
        Long id,

        @Schema(description = "첨부파일 이름", example = "filename.png")
        String filename,

        @Schema(description = "첨부파일 타입", example = "image/png")
        String type,

        @Schema(description = "첨부파일 크기", example = "33506")
        Long size,

        @Schema(description = "첨부파일 url", example = "https://img.presigned.url")
        String url,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "동영상 썸네일 url", example = "https://thumbnail.presigned.url")
        String thumbnailUrl,

        @Schema(description = "첨부파일 업로드 일시", example = "2025-06-22T23:26:17.366218")
        LocalDateTime uploadedAt

) {
    public static AttachmentResponse of(Attachment attachment, String url, String thumbnailUrl) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getContentType(),
                attachment.getSize(),
                url,
                thumbnailUrl,
                attachment.getCreatedAt()
        );
    }
}
