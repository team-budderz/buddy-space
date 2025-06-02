package team.budderz.buddyspace.api.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.comment.request.CommentRequest;
import team.budderz.buddyspace.api.comment.response.CommentResponse;
import team.budderz.buddyspace.api.comment.response.RecommentResponse;
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
    public BaseResponse<CommentResponse> saveComment (
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequest request,
            @AuthenticationPrincipal UserAuth userAuth
            ) {

        Long userId = userAuth.getUserId();
        CommentResponse response =  commentService.saveComment(groupId, postId, userId, request);
        return new BaseResponse<>(response);
    }

    // 대댓글 생성
    @PostMapping("/group/{groupId}/posts/{postId}/comments/{commentId}")
    public BaseResponse<RecommentResponse> saveComment (
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequest request,
            @AuthenticationPrincipal UserAuth userAuth
    ) {

        Long userId = userAuth.getUserId();
        RecommentResponse response =  commentService.saveRecomment(groupId, postId, commentId, userId, request);
        return new BaseResponse<>(response);
    }

}
