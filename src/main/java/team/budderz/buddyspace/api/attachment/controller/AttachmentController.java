package team.budderz.buddyspace.api.attachment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.attachment.response.AttachmentResponse;
import team.budderz.buddyspace.domain.attachment.service.AttachmentService;
import team.budderz.buddyspace.global.response.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

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

    @DeleteMapping
    public BaseResponse<Void> deleteAttachments(@RequestBody List<Long> attachmentIds) {
        attachmentService.deleteAttachments(attachmentIds);
        return new BaseResponse<>(null);
    }

    @DeleteMapping("/orphans")
    public BaseResponse<Integer> deleteOrphanAttachments() {
        Integer deleteSize = attachmentService.deleteOrphanAttachments();
        return new BaseResponse<>(deleteSize); // 삭제된 개수 반환
    }
}
