package team.budderz.buddyspace.api.attachment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.attachment.response.AttachmentResponse;
import team.budderz.buddyspace.domain.attachment.service.AttachmentService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    public BaseResponse<AttachmentResponse> upload(@RequestPart("file") MultipartFile file,
                                                   @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        AttachmentResponse response = attachmentService.upload(file, loginUserId);

        return new BaseResponse<>(response);
    }

    @GetMapping("/{attachmentId}")
    public BaseResponse<AttachmentResponse> getAttachmentDetail(@PathVariable Long attachmentId) {
        AttachmentResponse response = attachmentService.getAttachmentDetail(attachmentId);
        return new BaseResponse<>(response);
    }

    @GetMapping("/{attachmentId}/download")
    public BaseResponse<String> download(@PathVariable Long attachmentId) {
        String downloadUrl = attachmentService.getDownloadUrl(attachmentId);
        return new BaseResponse<>(downloadUrl);
    }

    @DeleteMapping("/{attachmentId}")
    public BaseResponse<Void> deleteAttachment(@PathVariable Long attachmentId) {
        attachmentService.delete(attachmentId);
        return new BaseResponse<>(null);
    }
}
