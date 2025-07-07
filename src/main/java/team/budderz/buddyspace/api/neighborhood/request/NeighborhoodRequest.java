package team.budderz.buddyspace.api.neighborhood.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "사용자 동네 인증 요청 DTO")
public record NeighborhoodRequest(
        @Schema(description = "사용자 위치 기반 위도", example = "37.5143449757")
        BigDecimal latitude,

        @Schema(description = "사용자 위치 기반 경도", example = "126.8976881922")
        BigDecimal longitude

) {
}
