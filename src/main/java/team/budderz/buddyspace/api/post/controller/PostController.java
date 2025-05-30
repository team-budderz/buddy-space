package team.budderz.buddyspace.api.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.post.request.SavePostRequest;
import team.budderz.buddyspace.api.post.request.UpdatePostRequest;
import team.budderz.buddyspace.api.post.response.SavePostResponse;
import team.budderz.buddyspace.api.post.response.UpdatePostResponse;
import team.budderz.buddyspace.domain.post.service.PostService;
import team.budderz.buddyspace.global.response.BaseResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping("/group/{groupId}/posts")
    public BaseResponse<SavePostResponse> savePost (
            @PathVariable Long groupId,
            @RequestBody SavePostRequest request
            ) {

        SavePostResponse response =  postService.savePost(groupId, request);
        return new BaseResponse<>(response);
    }

    // 게시글 수정
    @PatchMapping("/group/{groupId}/posts/{postId}")
    public BaseResponse<UpdatePostResponse> updatePost (
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request
            ) {

        UpdatePostResponse response = postService.updatePost(groupId, postId, request);
        return new BaseResponse<>(response);
    }

}
