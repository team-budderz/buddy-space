package team.budderz.buddyspace.api.neighborhood.response;

import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;

public record NeighborhoodResponse(
        Long neighborhoodId,
        String address,
        String cityName,
        String districtName,
        String wardName
) {
    public static NeighborhoodResponse from(Neighborhood neighborhood) {
        return new NeighborhoodResponse(
                neighborhood.getId(),
                neighborhood.getCityName() + " " + neighborhood.getDistrictName() + " " + neighborhood.getWardName(),
                neighborhood.getCityName(),
                neighborhood.getDistrictName(),
                neighborhood.getWardName()
        );
    }
}
