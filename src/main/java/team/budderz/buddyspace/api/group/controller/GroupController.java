package team.budderz.buddyspace.api.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team.budderz.buddyspace.api.group.request.SaveGroupRequest;
import team.budderz.buddyspace.api.group.request.UpdateGroupRequest;
import team.budderz.buddyspace.api.group.response.GroupListResponse;
import team.budderz.buddyspace.api.group.response.GroupResponse;
import team.budderz.buddyspace.domain.group.service.GroupService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.response.PageResponse;
import team.budderz.buddyspace.global.security.UserAuth;
import team.budderz.buddyspace.infra.database.group.entity.GroupSortType;

import java.util.List;

/**
 * 모임 기본 CRUD
 */
@Validated
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "모임 관리", description = "모임 관련 API")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "모임 생성", description = "새로운 모임을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "모임 생성 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<GroupResponse> saveGroup(
            @RequestPart("request") @Valid SaveGroupRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.saveGroup(loginUserId, request, coverImage);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 정보 수정", description = "기존 모임의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "모임 수정 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PutMapping(value = "/{groupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<GroupResponse> updateGroup(
            @PathVariable Long groupId,
            @RequestPart("request") @Valid UpdateGroupRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal UserAuth userAuth
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.updateGroup(loginUserId, groupId, request, coverImage);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 상세 조회", description = "특정 모임의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "모임 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/{groupId}")
    public BaseResponse<GroupResponse> findGroup(@PathVariable Long groupId,
                                                 @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.findGroup(groupId, loginUserId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 동네 재설정", description = "모임의 동네 주소를 리더의 현재 수조로 재설정합니다.")
    @ApiResponse(responseCode = "200", description = "모임 동네 재설정 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/{groupId}/address")
    public BaseResponse<GroupResponse> updateGroupAddress(@PathVariable Long groupId,
                                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response = groupService.updateGroupAddress(groupId, loginUserId);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "동네 인증 기반 가입 제한 설정",
            description = "오프라인 모임의 경우, 사용자의 동네 인증 여부에 따라 모임 가입 요청 가능 여부를 설정할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "동네 인증 기반 가입 제한 설정 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/{groupId}/neighborhood-auth-required")
    public BaseResponse<GroupResponse> updateGroupNeighborhoodAuthRequired(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestBody @NotNull Boolean neighborhoodAuthRequired
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupResponse response =
                groupService.updateGroupNeighborhoodAuthRequired(groupId, loginUserId, neighborhoodAuthRequired);

        return new BaseResponse<>(response);
    }

    @Operation(summary = "모임 삭제", description = "특정 모임을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "모임 삭제 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/{groupId}")
    public BaseResponse<Void> deleteGroup(@PathVariable Long groupId,
                                          @AuthenticationPrincipal UserAuth userAuth) {
        Long loginUserId = userAuth.getUserId();
        groupService.deleteGroup(loginUserId, groupId);

        return new BaseResponse<>(null);
    }

    @Operation(summary = "내가 속한 모임 조회", description = "로그인한 사용자가 참여 중인 모임 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/my")
    public BaseResponse<PageResponse<GroupListResponse>> findGroupsByUser(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam(defaultValue = "0") int page

    ) {
        Long loginUserId = userAuth.getUserId();
        PageResponse<GroupListResponse> responses = groupService.findGroupsByUser(loginUserId, page);

        return new BaseResponse<>(responses);
    }

    @Operation(summary = "내가 가입 요청한 모임 목록 조회", description = "로그인한 사용자가 가입 요청한 모임 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/my-requested")
    public BaseResponse<List<GroupResponse>> findMyRequested(@AuthenticationPrincipal UserAuth userAuth) {

        Long loginUserId = userAuth.getUserId();
        List<GroupResponse> responses = groupService.findMyRequested(loginUserId);

        return new BaseResponse<>(responses);
    }

    @Operation(summary = "온라인 모임 목록 조회", description = "온라인 모임을 정렬 기준과 관심사 기반으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/on")
    public BaseResponse<PageResponse<GroupListResponse>> findOnlineGroups(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam String sort,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupSortType sortType = GroupSortType.from(sort);
        PageResponse<GroupListResponse> responses = groupService.findOnlineGroups(loginUserId, sortType, interest, page);

        return new BaseResponse<>(responses);
    }

    @Operation(summary = "오프라인 모임 목록 조회",
            description = "오프라인 모임을 로그인한 사용자의 동네, 정렬 기준, 관심사 기반으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/off")
    public BaseResponse<PageResponse<GroupListResponse>> findOfflineGroups(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam String sort,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long loginUserId = userAuth.getUserId();
        GroupSortType sortType = GroupSortType.from(sort);
        PageResponse<GroupListResponse> responses = groupService.findOfflineGroups(loginUserId, sortType, interest, page);

        return new BaseResponse<>(responses);
    }

    @Operation(summary = "모임 이름 검색", description = "이름 키워드로 모임을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/search")
    public BaseResponse<PageResponse<GroupListResponse>> searchGroupsByName(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestParam String keyword,
            @RequestParam(required = false) String interest,
            @RequestParam(defaultValue = "0") int page
    ) {
        Long loginUserId = userAuth.getUserId();
        PageResponse<GroupListResponse> responses = groupService.searchGroupsByName(loginUserId, keyword, interest, page);

        return new BaseResponse<>(responses);
    }
}
