package team.budderz.buddyspace.api.attachment.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.attachment.entity.AttachmentType;

public record AttachmentResponse(
        Long id,
        String filename,
        AttachmentType type,
        Long size,
        String url,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String thumbnailUrl
) {
    public static AttachmentResponse of(Attachment attachment, String url, String thumbnailUrl) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFilename(),
                AttachmentType.fromContentType(attachment.getContentType()),
                attachment.getSize(),
                url,
                thumbnailUrl
        );
    }
}
