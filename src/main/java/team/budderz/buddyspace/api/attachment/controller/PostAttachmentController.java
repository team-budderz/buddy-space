package team.budderz.buddyspace.api.attachment.controller;

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
public class PostAttachmentController {

    private final PostAttachmentService postAttachmentService;

    @PostMapping("/post-files")
    public BaseResponse<AttachmentResponse> uploadPostFiles(@PathVariable Long groupId,
                                                            @RequestPart("file") MultipartFile file,
                                                            @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        AttachmentResponse response = postAttachmentService.uploadPostFiles(file, groupId, loginUserId);

        return new BaseResponse<>(response);
    }

    @GetMapping("/albums")
    public BaseResponse<List<AttachmentResponse>> findGroupAlbum(@PathVariable Long groupId,
                                                                 @RequestParam(required = false) String type,
                                                                 @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        List<AttachmentResponse> responses = postAttachmentService.findGroupAlbum(groupId, loginUserId, type);

        return new BaseResponse<>(responses);
    }
}
