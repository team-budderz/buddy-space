package team.budderz.buddyspace.api.attachment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.attachment.response.AttachmentResponse;
import team.budderz.buddyspace.domain.attachment.service.AttachmentService;
import team.budderz.buddyspace.global.response.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(name = "첨부파일 관리", description = "첨부파일 관련 API")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(
            summary = "첨부파일 상세 정보 조회",
            description = "첨부파일의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "첨부파일 상세 조회 성공")
            }
    )
    @GetMapping("/{attachmentId}")
    public BaseResponse<AttachmentResponse> getAttachmentDetail(@PathVariable Long attachmentId) {
        AttachmentResponse response = attachmentService.getAttachmentDetail(attachmentId);
        return new BaseResponse<>(response);
    }

    @Operation(
            summary = "첨부파일 다운로드 URL 생성",
            description = "첨부파일의 다운로드 URL을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "첨부파일 다운로드 URL 생성 성공")
            }
    )
    @GetMapping("/{attachmentId}/download")
    public BaseResponse<String> download(@PathVariable Long attachmentId) {
        String downloadUrl = attachmentService.getDownloadUrl(attachmentId);
        return new BaseResponse<>(downloadUrl);
    }

    @Operation(
            summary = "첨부파일 삭제",
            description = "특정 첨부파일을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "첨부파일 삭제 성공")
            }
    )
    @DeleteMapping("/{attachmentId}")
    public BaseResponse<Void> deleteAttachment(@PathVariable Long attachmentId) {
        attachmentService.delete(attachmentId);
        return new BaseResponse<>(null);
    }

    @Operation(
            summary = "첨부파일 일괄 삭제",
            description = "첨부파일의 식별자를 리스트로 받아 일괄 삭제합니다. 게시글 작성 중 취소 시 사용됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "첨부파일 일괄 삭제 성공")
            }
    )
    @DeleteMapping
    public BaseResponse<Void> deleteAttachments(@RequestBody List<Long> attachmentIds) {
        attachmentService.deleteAttachments(attachmentIds);
        return new BaseResponse<>(null);
    }

    @Operation(
            summary = "고아 첨부파일 일괄 삭제",
            description = "사용자, 모임, 게시글에서 사용되지 않는 고아 첨부파일을 일괄 삭제합니다. (관리자용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "고아 첨부파일 일괄 삭제 성공")
            }
    )
    @DeleteMapping("/orphans")
    public BaseResponse<Integer> deleteOrphanAttachments() {
        Integer deleteSize = attachmentService.deleteOrphanAttachments();
        return new BaseResponse<>(deleteSize); // 삭제된 개수 반환
    }
}
