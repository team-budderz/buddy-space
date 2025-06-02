package team.budderz.buddyspace.api.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.comment.request.SaveCommentRequest;
import team.budderz.buddyspace.api.comment.response.SaveCommentResponse;
import team.budderz.buddyspace.domain.comment.service.CommentService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("/group/{groupId}/posts/{postId}/comments")
    public BaseResponse<SaveCommentResponse> saveComment (
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestBody @Valid SaveCommentRequest request,
            @AuthenticationPrincipal UserAuth userAuth
            ) {

        Long userId = userAuth.getUserId();
        SaveCommentResponse response =  commentService.savePost(groupId, postId, userId, request);
        return new BaseResponse<>(response);
    }

}
