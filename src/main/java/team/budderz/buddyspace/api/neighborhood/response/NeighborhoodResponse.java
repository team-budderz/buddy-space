package team.budderz.buddyspace.api.neighborhood.response;

import io.swagger.v3.oas.annotations.media.Schema;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;

@Schema(description = "사용자 동네 인증 응답 DTO")
public record NeighborhoodResponse(
        @Schema(description = "사용자 동네 인증 식별자", example = "2")
        Long neighborhoodId,

        @Schema(description = "사용자 위치 기반 주소", example = "서울 영등포구 문래동")
        String address,

        @Schema(description = "사용자 위치 기반 시 or 도", example = "서울")
        String cityName,

        @Schema(description = "사용자 위치 기반 구 or 시/군", example = "영등포구")
        String districtName,

        @Schema(description = "사용자 위치 기반 동/읍/면/리", example = "문래동")
        String wardName

) {
    public static NeighborhoodResponse from(Neighborhood neighborhood) {
        return new NeighborhoodResponse(
                neighborhood.getId(),
                neighborhood.getVerifiedAddress(),
                neighborhood.getCityName(),
                neighborhood.getDistrictName(),
                neighborhood.getWardName()
        );
    }
}
