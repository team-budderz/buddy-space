package team.budderz.buddyspace.api.attachment.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Long id,
        String filename,
        String type,
        Long size,
        String url,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String thumbnailUrl,
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
