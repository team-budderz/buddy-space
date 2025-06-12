package team.budderz.buddyspace.infra.database.neighborhood.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;

import java.util.Optional;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {
    Optional<Neighborhood> findByCityNameAndDistrictNameAndWardName(String cityName, String districtName, String wardName);
}
