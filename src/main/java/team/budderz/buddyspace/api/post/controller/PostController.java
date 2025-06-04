package team.budderz.buddyspace.api.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.post.request.SavePostRequest;
import team.budderz.buddyspace.api.post.request.UpdatePostRequest;
import team.budderz.buddyspace.api.post.response.FindsNoticePostResponse;
import team.budderz.buddyspace.api.post.response.FindsPostResponse;
import team.budderz.buddyspace.api.post.response.SavePostResponse;
import team.budderz.buddyspace.api.post.response.UpdatePostResponse;
import team.budderz.buddyspace.domain.post.service.PostService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping("/group/{groupId}/posts")
    public BaseResponse<SavePostResponse> savePost (
            @PathVariable Long groupId,
            @RequestBody @Valid SavePostRequest request,
            @AuthenticationPrincipal UserAuth userAuth
            ) {
        Long userId = userAuth.getUserId();
        SavePostResponse response =  postService.savePost(groupId, userId, request);
        return new BaseResponse<>(response);
    }

    // 게시글 수정
    @PatchMapping("/group/{groupId}/posts/{postId}")
    public BaseResponse<UpdatePostResponse> updatePost (
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestBody @Valid UpdatePostRequest request,
            @AuthenticationPrincipal UserAuth userAuth
            ) {
        Long userId = userAuth.getUserId();
        UpdatePostResponse response = postService.updatePost(groupId, postId, userId, request);
        return new BaseResponse<>(response);
    }

    // 게시글 삭제
    @DeleteMapping("/group/{groupId}/posts/{postId}")
    public BaseResponse<String> deletePost (
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long userId = userAuth.getUserId();
        postService.deletePost(groupId, postId, userId);
        return new BaseResponse<>("게시글이 성공적으로 삭제되었습니다.");
    }

    // 게시글 전체 조회
    @GetMapping("/group/{groupId}/posts")
    public BaseResponse<List<FindsPostResponse>> findsPost (
            @PathVariable Long groupId
    ) {
        List<FindsPostResponse> response = postService.findsPost(groupId);
        return new BaseResponse<>(response);
    }

    // 게시글 공지 조회(내용 일부)
    @GetMapping("/group/{groupId}/posts-notice")
    public BaseResponse<List<FindsNoticePostResponse>> findNoticePostSummaries(
            @PathVariable Long groupId
    ) {
        List<FindsNoticePostResponse> response = postService.findNoticePostSummaries(groupId);
        return new BaseResponse<>(response);
    }

}
