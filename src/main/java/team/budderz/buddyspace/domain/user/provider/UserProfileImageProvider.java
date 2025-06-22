package team.budderz.buddyspace.domain.user.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.attachment.response.AttachmentResponse;
import team.budderz.buddyspace.domain.attachment.service.AttachmentService;
import team.budderz.buddyspace.infra.client.s3.S3Directory;
import team.budderz.buddyspace.infra.client.s3.DefaultImageProvider;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Component
@RequiredArgsConstructor
public class UserProfileImageProvider {

    private final DefaultImageProvider defaultImageProvider;
    private final AttachmentService attachmentService;

    public Attachment getProfileAttachment(MultipartFile profile, Long userId) {
        if (profile == null || profile.isEmpty()) {
            return null; // 기본 이미지인 경우 null
        }
        AttachmentResponse uploaded = attachmentService.upload(profile, userId, S3Directory.PROFILE);
        return attachmentService.findAttachmentOrThrow(uploaded.id());
    }

    public String getProfileImageUrl(User user) {
        Attachment profileAttachment = user.getProfileAttachment();

        if (profileAttachment == null) {
            return defaultImageProvider.getDefaultProfileImageUrl();
        }
        return attachmentService.getViewUrl(profileAttachment.getId());
    }

    public String getProfileImageUrl(@Nullable Long profileAttachmentId) {
        if (profileAttachmentId == null) {
            return defaultImageProvider.getDefaultProfileImageUrl();
        }
        Attachment profileAttachment = attachmentService.findAttachmentOrThrow(profileAttachmentId);
        return attachmentService.getViewUrl(profileAttachment.getId());
    }
}
