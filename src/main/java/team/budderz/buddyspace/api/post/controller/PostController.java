package team.budderz.buddyspace.api.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.post.request.SavePostRequest;
import team.budderz.buddyspace.api.post.response.SavePostResponse;
import team.budderz.buddyspace.domain.post.service.PostService;
import team.budderz.buddyspace.global.response.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/group/{groupId}/posts")
    public BaseResponse<SavePostResponse> savePost (
            @RequestParam Long groupId,
            @RequestBody SavePostRequest request
            ) {

        SavePostResponse response =  postService.savePost(groupId, request);
        return new BaseResponse<>(response);
    }

}
