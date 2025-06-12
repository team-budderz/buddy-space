package team.budderz.buddyspace.api.neighborhood.controller;

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

public class NeighborhoodController {

    private final NeighborhoodService neighborhoodService;

    @PostMapping
    public BaseResponse<NeighborhoodResponse> saveNeighborhood(
            @AuthenticationPrincipal UserAuth userAuth,
            @RequestBody NeighborhoodRequest request
    ) {
        return new BaseResponse<>(neighborhoodService.saveNeighborhood(userAuth.getUserId(), request));
    }

    @GetMapping("/{neighborhoodId}")
    public BaseResponse<NeighborhoodResponse> findNeighborhood(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long neighborhoodId
    ) {
        return new BaseResponse<>(neighborhoodService.findNeighborhood(userAuth.getUserId(), neighborhoodId));
    }

    @DeleteMapping("/{neighborhoodId}")
    public BaseResponse<Void> deleteNeighborhood(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long neighborhoodId
    ) {
        neighborhoodService.deleteNeighborhood(userAuth.getUserId(), neighborhoodId);
        return new BaseResponse<>(null);
    }
}
