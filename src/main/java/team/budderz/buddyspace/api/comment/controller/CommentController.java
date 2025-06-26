package team.budderz.buddyspace.api.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.comment.request.CommentRequest;
import team.budderz.buddyspace.api.comment.response.CommentResponse;
import team.budderz.buddyspace.api.comment.response.FindsRecommentResponse;
import team.budderz.buddyspace.api.comment.response.RecommentResponse;
import team.budderz.buddyspace.domain.comment.service.CommentService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("/groups/{groupId}/posts/{postId}/comments")
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
    @PostMapping("/groups/{groupId}/posts/{postId}/comments/{commentId}")
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

    // 댓글 수정 (대댓글 포함)
    @PatchMapping("/groups/{groupId}/posts/{postId}/comments/{commentId}")
    public BaseResponse<CommentResponse> updateComment (
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequest request,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long userId = userAuth.getUserId();
        CommentResponse response =  commentService.updateComment(groupId, postId, commentId, userId, request);
        return new BaseResponse<>(response);
    }

     // 댓글 삭제
    @DeleteMapping("/groups/{groupId}/posts/{postId}/comments/{commentId}")
    public BaseResponse<String> deletecomment (
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long userId = userAuth.getUserId();
        commentService.deleteComment(groupId, postId, commentId, userId);
        return new BaseResponse<>("댓글이 성공적으로 삭제되었습니다.");
    }

    // 대댓글 조회
    @GetMapping("/groups/{groupId}/posts/{postId}/comments/{commentId}")
    public BaseResponse<List<FindsRecommentResponse>> findsRecomment(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        List<FindsRecommentResponse> responses = commentService.findsRecomment(groupId, postId, commentId);
        return new BaseResponse<>(responses);
    }

}
