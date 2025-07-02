package team.budderz.buddyspace.api.neighborhood.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.neighborhood.request.NeighborhoodRequest;
import team.budderz.buddyspace.api.neighborhood.response.NeighborhoodResponse;
import team.budderz.buddyspace.domain.neighborhood.service.NeighborhoodService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neighborhoods")
@Tag(name = "동네 인증 관리", description = "동네 인증 관련 API")
public class NeighborhoodController {

    private final NeighborhoodService neighborhoodService;

    @Operation(summary = "사용자 동네 인증", description = "로그인한 사용자의 현재 위치를 기반으로 동네 정보를 저장합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 동네 인증 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping
    public BaseResponse<NeighborhoodResponse> saveNeighborhood(@AuthenticationPrincipal UserAuth userAuth,
                                                               @RequestBody NeighborhoodRequest request) {
        NeighborhoodResponse response = neighborhoodService.saveNeighborhood(userAuth.getUserId(), request);
        return new BaseResponse<>(response);
    }

    @Operation(summary = "사용자 동네 인증 정보 조회", description = "로그인한 사용자의 동네 인증 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 동네 인증 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/{neighborhoodId}")
    public BaseResponse<NeighborhoodResponse> findNeighborhood(@AuthenticationPrincipal UserAuth userAuth,
                                                               @PathVariable Long neighborhoodId) {
        NeighborhoodResponse response = neighborhoodService.findNeighborhood(userAuth.getUserId(), neighborhoodId);
        return new BaseResponse<>(response);
    }

    @Operation(summary = "사용자 동네 인증 정보 삭제", description = "로그인한 사용자의 동네 인증 정보를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 동네 인증 정보 삭제 성공",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/{neighborhoodId}")
    public BaseResponse<Void> deleteNeighborhood(@AuthenticationPrincipal UserAuth userAuth,
                                                 @PathVariable Long neighborhoodId) {
        neighborhoodService.deleteNeighborhood(userAuth.getUserId(), neighborhoodId);
        return new BaseResponse<>(null);
    }
}
