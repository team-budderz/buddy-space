package team.budderz.buddyspace.api.attachment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.attachment.response.AttachmentResponse;
import team.budderz.buddyspace.domain.attachment.service.PostAttachmentService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
@Tag(name = "모임 내 첨부파일 관리", description = "모임 내 첨부파일 관련 API")
public class PostAttachmentController {

    private final PostAttachmentService postAttachmentService;

    @Operation(summary = "게시글 작성 중 첨부파일 업로드",
            description = "게시글 작성 중 파일이 첨부되었을 때 호출됩니다. 첨부된 파일을 즉시 업로드합니다.")
    @ApiResponse(responseCode = "200", description = "첨부파일 업로드 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping("/post-files")
    public BaseResponse<AttachmentResponse> uploadPostFiles(@PathVariable Long groupId,
                                                            @RequestPart("file") MultipartFile file,
                                                            @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        AttachmentResponse response = postAttachmentService.uploadPostFiles(file, groupId, loginUserId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 사진첩 조회", description = "특정 모임의 모든 게시글에 첨부된 사진과 영상 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "모임 사진첩 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/albums")
    public BaseResponse<List<AttachmentResponse>> findGroupAlbum(@PathVariable Long groupId,
                                                                 @RequestParam(required = false) String type,
                                                                 @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        List<AttachmentResponse> responses = postAttachmentService.findGroupAlbum(groupId, loginUserId, type);

        return new BaseResponse<>(responses);
    }
}
