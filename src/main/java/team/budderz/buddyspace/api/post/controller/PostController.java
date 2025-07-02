package team.budderz.buddyspace.api.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.post.request.SavePostRequest;
import team.budderz.buddyspace.api.post.request.UpdatePostRequest;
import team.budderz.buddyspace.api.post.response.*;
import team.budderz.buddyspace.domain.post.service.PostService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "게시글", description = "게시글 관련 API")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 생성 성공",
            content = @Content(schema = @Schema(implementation = SavePostResponse.class)))
    @PostMapping("/groups/{groupId}/posts")
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
    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
            content = @Content(schema = @Schema(implementation = UpdatePostResponse.class)))
    @PatchMapping("/groups/{groupId}/posts/{postId}")
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
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 삭제 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/groups/{groupId}/posts/{postId}")
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
    @Operation(summary = "게시글 전체 조회", description = "해당 그룹의 전체 게시글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 조회 성공",
            content = @Content(schema = @Schema(implementation = FindsPostResponse.class)))
    @GetMapping("/groups/{groupId}/posts")
    public BaseResponse<List<FindsPostResponse>> findsPost (
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page
    ) {
        List<FindsPostResponse> response = postService.findsPost(groupId, page);
        return new BaseResponse<>(response);
    }

    // 게시글 공지 조회(내용 일부)
    @Operation(summary = "공지 게시글 요약 조회", description = "공지 게시글의 요약 내용을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 게시글 요약 조회 성공",
            content = @Content(schema = @Schema(implementation = FindsNoticePostResponse.class)))
    @GetMapping("/groups/{groupId}/posts-notice")
    public BaseResponse<List<FindsNoticePostResponse>> findNoticePostSummaries(
            @PathVariable Long groupId
    ) {
        List<FindsNoticePostResponse> response = postService.findNoticePostSummaries(groupId);
        return new BaseResponse<>(response);
    }

    // 게시글 공지 조회
    @Operation(summary = "공지 게시글 전체 조회", description = "공지 게시글 전체 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 게시글 전체 조회 성공",
            content = @Content(schema = @Schema(implementation = FindsPostResponse.class)))
    @GetMapping("/groups/{groupId}/posts/notice")
    public BaseResponse<List<FindsPostResponse>> findsNoticePost (
            @PathVariable Long groupId
    ) {
        List<FindsPostResponse> response = postService.findsNoticePost(groupId);
        return new BaseResponse<>(response);
    }

    // 게시글 상세 조회
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = FindPostResponse.class)))
    @GetMapping("/groups/{groupId}/posts/{postId}")
    public BaseResponse<FindPostResponse> findPost(
            @PathVariable Long groupId,
            @PathVariable Long postId
    ) {
        FindPostResponse response = postService.findPost(groupId, postId);
        return new BaseResponse<>(response);
    }
}
