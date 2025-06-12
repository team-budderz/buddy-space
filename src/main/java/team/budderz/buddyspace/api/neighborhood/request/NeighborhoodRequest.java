package team.budderz.buddyspace.api.neighborhood.request;

import java.math.BigDecimal;

public record NeighborhoodRequest(
        BigDecimal latitude,
        BigDecimal longitude
) {
}
